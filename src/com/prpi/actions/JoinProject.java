package com.prpi.actions;

import com.intellij.ide.impl.NewProjectUtil;
import com.intellij.ide.projectWizard.NewProjectWizard;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import com.intellij.openapi.ui.Messages;
import com.prpi.PrPiApplicationComponent;
import com.prpi.PrPiProjectComponent;
import org.apache.log4j.Logger;

public class JoinProject extends AnAction {

    private static final Logger logger = Logger.getLogger(JoinProject.class);

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        logger.trace("JoinProject actionPerformed begin");

        NewProjectWizard wizard = new NewProjectWizard(null, ModulesProvider.EMPTY_MODULES_PROVIDER, null);
        NewProjectUtil.createNewProject(anActionEvent.getProject(), wizard);

        logger.trace("JoinProject actionPerformed end");
    }

    @Override
    public void update(AnActionEvent anActionEvent) {
        super.update(anActionEvent);
        anActionEvent.getPresentation().setEnabled(!anActionEvent.getProject().getComponent(PrPiProjectComponent.class).isHosting());
    }
}
