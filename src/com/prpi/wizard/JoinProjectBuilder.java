package com.prpi.wizard;

import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import com.prpi.PrPiApplicationComponent;
import com.prpi.network.PrPiClient;
import org.jetbrains.annotations.NotNull;

public class JoinProjectBuilder extends ModuleBuilder {

    @Override
    public void setupRootModel(ModifiableRootModel modifiableRootModel) throws ConfigurationException {
        PrPiClient client = PrPiApplicationComponent.getPrPiAppComp(null).getClientThread();
        client.setCurrentProject(modifiableRootModel.getProject());
        client.run();
        modifiableRootModel.commit();
    }

    @Override
    public ModuleType getModuleType() {
        return JoinProjectModule.getInstance();
    }

    @Override
    public ModuleWizardStep[] createWizardSteps(@NotNull WizardContext wizardContext, @NotNull ModulesProvider modulesProvider) {
        return new ModuleWizardStep[] {
                new JoinProjectInputIPAndPortStep()
        };
    }
}

