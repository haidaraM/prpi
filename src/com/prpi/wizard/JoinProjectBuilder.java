package com.prpi.wizard;

import com.intellij.ide.util.projectWizard.ExistingModuleLoader;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import com.prpi.PrPiProjectComponent;
import com.prpi.network.client.PrPiClient;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

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
        PrPiProjectComponent projectApp = dest.getComponent(PrPiProjectComponent.class);
        PrPiClient client = new PrPiClient(hostname, port);
        client.setCurrentProject(dest);
        projectApp.setClientThread(client);
        logger.debug("Begin init client - Copy files from server");
        if (client.initConnection()) {
            if (this.wizardContext != null) {
                this.wizardContext.setProjectName(client.getProjectNameToSet());
                // TODO Disable last wizard step and set project name and properties (not with iml like this...)
            }
            return super.validate(current, dest);
        }
        return false;
    }

    public void setHostnameAndPort(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }
}

