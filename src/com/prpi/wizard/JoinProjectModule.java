package com.prpi.wizard;

import com.intellij.icons.AllIcons;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.ModuleTypeManager;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class JoinProjectModule extends ModuleType<JoinProjectBuilder> {
    private static final String ID = "JOIN_PROJECT";

    public JoinProjectModule() {
        super(ID);
    }

    public static JoinProjectModule getInstance() {
        return (JoinProjectModule) ModuleTypeManager.getInstance().findByID(ID);
    }

    @NotNull
    @Override
    public JoinProjectBuilder createModuleBuilder() {
        return new JoinProjectBuilder();
    }

    @NotNull
    @Override
    public String getName() {
        return "Join Shared Project";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Join Project";
    }

    @Override
    public Icon getBigIcon() {
        return AllIcons.General.Information;
    }

    @Override
    public Icon getNodeIcon(@Deprecated boolean b) {
        return AllIcons.General.Information;
    }

    @NotNull
    @Override
    public ModuleWizardStep[] createWizardSteps(@NotNull WizardContext wizardContext, @NotNull JoinProjectBuilder moduleBuilder, @NotNull ModulesProvider modulesProvider) {
        return super.createWizardSteps(wizardContext, moduleBuilder, modulesProvider);
    }
}