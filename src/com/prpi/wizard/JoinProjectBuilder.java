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
            try {
                client.downloadProjetFiles();
            } catch (InterruptedException e) {
                logger.error(e);
            }

            Task.Modal modalTask = new Task.Modal(dest, "Download project files", true) {
                @Override
                public void run(@NotNull ProgressIndicator progressIndicator) {

                    // TODO get real value
                    for (int i = 0 ; i < 100; i++){
                        progressIndicator.setText("Downloading files ... (file " + i + ")");
                        progressIndicator.setFraction(i/100);
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        progressIndicator.checkCanceled();
                    }
                }

                @Override
                public void onSuccess() {
                    super.onSuccess();
                    Messages.showMessageDialog(getProject(),"Success","Success",Messages.getInformationIcon());
                }

                @Override
                public void onCancel() {
                    super.onCancel();
                    // How to canceled the validation ? Need to return false, impossible here ?
                    Messages.showMessageDialog(getProject(),"Cancel","Nodal Canceled",Messages.getQuestionIcon());
                }
            };

            ProgressManager.getInstance().run(modalTask);

            // TODO change return value with the Task.Modal result
            return true;
        } else {
            logger.error("Client can't connect to the remote server !");
        }
        return false;
    }
}

