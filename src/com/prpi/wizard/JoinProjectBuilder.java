package com.prpi.wizard;

import com.intellij.ide.util.projectWizard.ExistingModuleLoader;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import com.prpi.PrPiApplicationComponent;
import com.prpi.network.PrPiClient;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class JoinProjectBuilder extends ExistingModuleLoader {

    private static final Logger logger = Logger.getLogger(JoinProjectBuilder.class);
    private Project myProject;

    @Override
    public ModuleWizardStep[] createWizardSteps(@NotNull WizardContext wizardContext, @NotNull ModulesProvider modulesProvider) {
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

    public Project getMyProject() {
        return myProject;
    }

    public void setMyProject(Project myProject) {
        this.myProject = myProject;
    }

    @Override
    public boolean validate(final Project current, final Project dest) {
        PrPiClient client = PrPiApplicationComponent.getPrPiAppComp(dest).getClientThread();
        client.setCurrentProject(dest);
        logger.debug("Begin of init client");
        client.initConnection();
        logger.debug("End of init client - Wait");

        // TODO Need to wait the receive files - This is for test
        for(int i=0; i < 10000; i++) {
            for(int j=0; j < 1000000; j++) {

            }
            logger.debug("Wait " + i + " / " + 1000000);
        }
        logger.debug("End of waiting");
        return super.validate(current, dest);
    }
}

