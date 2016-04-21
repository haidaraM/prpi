package com.prpi.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.project.Project;
import com.prpi.ProjectComponent;
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
        boolean isHosting;

        Project p = anActionEvent.getProject();
        if (p == null) {
            isHosting = false;
        } else {
            ProjectComponent pc = p.getComponent(ProjectComponent.class);
            isHosting = (pc != null && pc.isHosting());
        }

        anActionEvent.getPresentation().setEnabled(isHosting);
    }
}
