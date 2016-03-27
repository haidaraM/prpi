package com.prpi.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.AnAction;
import com.prpi.PrPiApplicationComponent;
import com.prpi.PrPiProjectComponent;
import org.apache.log4j.Logger;

public class EditProjectConfiguration extends AnAction {

    private static final Logger logger = Logger.getLogger(EditProjectConfiguration.class);

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        logger.trace("EditProjectConfiguration actionPerformed begin");

        logger.trace("EditProjectConfiguration actionPerformed end");
    }

    @Override
    public void update(AnActionEvent anActionEvent) {
        super.update(anActionEvent);
        anActionEvent.getPresentation().setEnabled(anActionEvent.getProject().getComponent(PrPiProjectComponent.class).isHosting());
    }
}
