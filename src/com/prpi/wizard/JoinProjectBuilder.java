package com.prpi.wizard;

import com.intellij.ide.util.projectWizard.ExistingModuleLoader;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.ui.DialogWrapper;
import com.prpi.ProjectComponent;
import com.prpi.network.client.Client;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

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


            DialogBuilder dialogBuilder = new DialogBuilder(dest);
            dialogBuilder.removeAllActions();
            dialogBuilder.addCancelAction();
            dialogBuilder.setTitle("Download project files");

            JPanel dialogPanel = new JPanel();
            dialogPanel.setLayout(new BoxLayout(dialogPanel, BoxLayout.Y_AXIS));

            final int progressBarMaxValue = 100;
            JProgressBar progressBar = new JProgressBar(0, progressBarMaxValue);
            progressBar.setValue(50);
            dialogPanel.add(progressBar);

            dialogPanel.add(new JLabel("Downloading files from the remote project ..."));

            Runnable progressTask = new Runnable() {
                public void run() {
                    while (progressBar.getValue() < progressBarMaxValue) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        progressBar.setValue(progressBar.getValue()+1);
                    }
                }
            };

            dialogBuilder.setCenterPanel(dialogPanel);
            //progressTask.run();

            return dialogBuilder.show() == DialogWrapper.OK_EXIT_CODE;
        } else {
            logger.error("Client can't connect to the remote server !");
        }
        return false;
    }
}

