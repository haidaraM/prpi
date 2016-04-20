package com.prpi.wizard;

import com.intellij.ide.util.projectWizard.ExistingModuleLoader;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.ProgressManagerQueue;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.progress.impl.ProgressManagerImpl;
import com.intellij.openapi.progress.util.ProgressIndicatorListenerAdapter;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.prpi.ProjectComponent;
import com.prpi.network.client.Client;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;

public class JoinProjectBuilder extends ExistingModuleLoader {

    private static final Logger logger = Logger.getLogger(JoinProjectBuilder.class);

    private String hostname;
    private int port;
    private WizardContext wizardContext;

    @Override
    public ModuleWizardStep[] createWizardSteps(@NotNull WizardContext wizardContext, @NotNull ModulesProvider modulesProvider) {
        this.wizardContext = wizardContext;
        return new ModuleWizardStep[] {
                new JoinProjectInputIPAndPortStep(this, wizardContext)
        };
    }

    @Override
    public ModuleType getModuleType() {
        return JoinProjectModule.getInstance();
    }

    @Override
    public void setupRootModel(ModifiableRootModel rootModel) throws com.intellij.openapi.options.ConfigurationException {
        // Empty
    }

    @Override
    public boolean validate(final Project current, final Project dest) {
        return this.initClientAndProject(dest) && super.validate(current, dest);
    }

    public void setHostnameAndPort(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    private boolean initClientAndProject(final Project dest) {

        ProjectComponent projectApp = dest.getComponent(ProjectComponent.class);
        Client client = new Client(dest);
        projectApp.setClient(client);
        logger.debug("Begin init client");

        if (client.connect(hostname, port)) {
            int projectSize = -1;
            try {
                projectSize = client.downloadProjetFiles();
            } catch (InterruptedException e) {
                logger.error(e);
            }

            final boolean[] resultDownloadTask = {false};
            final int finalProjectSize = projectSize;

            Task.Modal modalTask = new Task.Modal(dest, "Download project files", true) {
                @Override
                public void run(@NotNull ProgressIndicator progressIndicator) {
                    if (finalProjectSize != -1) {
                        // TODO get real value
                        for (int i = 0; i < finalProjectSize; i++){
                            progressIndicator.setText("Downloading files ... ");
                            progressIndicator.setText2("Size " + i + " / " + finalProjectSize);
                            progressIndicator.setFraction(i/finalProjectSize);
                            try {
                                Thread.sleep(10);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            progressIndicator.checkCanceled();
                        }
                    } else {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onSuccess() {
                    super.onSuccess();
                    resultDownloadTask[0] = true;
                }

                @Override
                public void onCancel() {
                    super.onCancel();
                    resultDownloadTask[0] = false;
                    Messages.showWarningDialog(getProject(), "You canceled the download process, the project can't be initialized.", "Download Canceled");
                    // TODO delete download files
                }
            };

            ProgressManager.getInstance().run(modalTask);

            return resultDownloadTask[0];
        } else {
            logger.error("Client can't connect to the remote server !");
        }
        return false;
    }
}

