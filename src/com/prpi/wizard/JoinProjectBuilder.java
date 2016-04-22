package com.prpi.wizard;

import com.intellij.ide.projectWizard.ProjectSettingsStep;
import com.intellij.ide.util.projectWizard.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
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
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
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

            final String[] errorMsg = {null};
            final String[] errorTitle = {null};
            final boolean[] resultDownloadTask = {false};

            Task.Modal modalTask = new Task.Modal(dest, "Download project files", true) {
                @Override
                public void run(@NotNull ProgressIndicator progressIndicator) {
                    progressIndicator.setText("Authentification ...");
                    boolean authentification = false;
                    try {
                        authentification = client.identification(InetAddress.getLocalHost().getHostName());
                    } catch (InterruptedException | TimeoutException | UnknownHostException e) {
                        logger.error(e);
                    }
                    if (!authentification) {
                        errorMsg[0] = "Your authentification failed, the project can't be initialized.";
                        errorTitle[0] = "Identification Failed";
                        return;
                    }

                    int nbFilesToDwnload = -1;
                    progressIndicator.setText("Authentification accepted !");
                    progressIndicator.setText2("Get number of files to download ...");
                    try {
                        nbFilesToDwnload = client.downloadProjetFiles();
                    } catch (InterruptedException | TimeoutException e) {
                        logger.error(e);
                    }
                    if (nbFilesToDwnload > -1) {
                        for (int i = 0; i < nbFilesToDwnload;){
                            progressIndicator.setText("Downloading files ... ");
                            progressIndicator.setText2("File " + i + " / " + nbFilesToDwnload);
                            progressIndicator.setFraction((double)i/(double)nbFilesToDwnload);
                            try {
                                Thread.sleep(1000);
                                i = client.getCurrentProjectSize();
                            } catch (InterruptedException e) {
                                logger.error(e);
                            }
                            progressIndicator.checkCanceled();
                        }
                    } else {
                        errorMsg[0] = "Error during fetching data to download, the project can't be initialized.";
                        errorTitle[0] = "Download Error";
                        return;
                    }
                    resultDownloadTask[0] = true;
                }

                @Override
                public void onCancel() {
                    super.onCancel();
                    resultDownloadTask[0] = false;
                    errorMsg[0] = "You canceled the download process, the project can't be initialized.";
                    errorTitle[0] = "Download Canceled";
                    // TODO delete download files
                }
            };

            ProgressManager.getInstance().run(modalTask);

            if (!resultDownloadTask[0]) {
                client.close();
                ApplicationManager.getApplication().invokeLater(() -> Messages.showErrorDialog(dest, errorMsg[0], errorTitle[0]));
            }

            return resultDownloadTask[0];
        } else {
            logger.error("Client can't connect to the remote server !");
        }
        return false;
    }
}
