package com.prpi.wizard;

import com.intellij.ide.projectWizard.ProjectSettingsStep;
import com.intellij.ide.util.projectWizard.*;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import com.intellij.openapi.ui.Messages;
import com.prpi.ProjectComponent;
import com.prpi.network.client.Client;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class JoinProjectBuilder extends ExistingModuleLoader {

    private static final Logger logger = Logger.getLogger(JoinProjectBuilder.class);

    private String projectName;
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
    public boolean validate(final Project current, final Project dest) {
        return initClientAndProject(current, dest) && super.validate(current, dest);
    }

    @Nullable
    @Override
    public ModuleWizardStep modifySettingsStep(@NotNull SettingsStep settingsStep) {
        logger.debug("Modify settings step " + settingsStep.getClass());

        ProjectSettingsStep p = (ProjectSettingsStep) settingsStep;
        p.getModuleNameField().setEnabled(false);
        p.getModuleNameField().setText(projectName);

        return super.modifySettingsStep(p);
    }

    @Override
    public void setupRootModel(ModifiableRootModel modifiableRootModel) throws ConfigurationException {
        logger.trace("Setup root model - does nothing");
    }

    @Override
    public ModuleType getModuleType() {
        return JoinProjectModule.getInstance();
    }

    void setHostnameAndPort(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    private boolean initClientAndProject(final Project current, final Project dest) {

        ProjectComponent projectApp = dest.getComponent(ProjectComponent.class);
        Client client = new Client(dest);
        projectApp.setClient(client);
        logger.debug("Begin init client");

        if (client.connect(hostname, port)) {
            final boolean[] resultDownloadTask = {false};

            Task.Modal modalTask = new Task.Modal(dest, "Download project files", true) {
                @Override
                public void run(@NotNull ProgressIndicator progressIndicator) {
                    int projectSize = -1;
                    progressIndicator.setText2("Get number of files to download ...");
                    try {
                        projectSize = client.downloadProjetFiles();
                    } catch (InterruptedException | TimeoutException e) {
                        logger.error(e);
                    }
                    if (projectSize != -1) {
                        for (int i = 0; i < projectSize;){
                            progressIndicator.setText("Downloading files ... ");
                            progressIndicator.setText2("File " + i + " / " + projectSize);
                            progressIndicator.setFraction((double)i/(double)projectSize);
                            try {
                                Thread.sleep(1000);
                                i = client.getCurrentProjectSize();
                            } catch (InterruptedException e) {
                                logger.error(e);
                            }
                            progressIndicator.checkCanceled();
                        }
                    } else {
                        progressIndicator.setText("Downloading files ... ");
                        progressIndicator.setText2("Can't get information from remote server ...");
                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException e) {
                            logger.error(e);
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
