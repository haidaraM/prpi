package com.prpi.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.prpi.ProjectComponent;
import com.prpi.network.server.Server;
import org.apache.log4j.Logger;

public class HostProject extends AnAction {
    private static final Logger logger = Logger.getLogger(HostProject.class);

    private Project project = null;
    private ProjectComponent projectComponent = null;

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        logger.trace("HostProject actionPerformed called");
        try {
            project = anActionEvent.getProject();
            projectComponent = project.getComponent(ProjectComponent.class);
            if (projectComponent.isClient()) {
                Messages.showWarningDialog(project, "You are already a client of this remote project, you can't hosting this project!", "PrPi Warning - Host Is Not Allow");
            } else if (projectComponent.isHosting()) {
                Messages.showWarningDialog(project, "You already host this project!", "PrPi Warning - Project Already Hosted");
            } else {
                // TODO : add window for choosing port
                launchServer(Server.DEFAULT_PORT);
                logger.debug("Project hosted - Server start");
            }
        } catch (Exception ex) {
            logger.error("Error launching server", ex);
            Messages.showErrorDialog(anActionEvent.getProject(), ex.getMessage(), "Error Starting Server Thread");
        }
        logger.trace("HostProject actionPerformed end");
    }

    private void launchServer(int port) {
        Server server = new Server(project, port);
        server.start();
        projectComponent.setServer(server);
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
        anActionEvent.getPresentation().setEnabled(!isHosting);
    }
}
