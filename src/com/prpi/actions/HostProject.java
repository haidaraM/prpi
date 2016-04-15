package com.prpi.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.prpi.PrPiProjectComponent;
import com.prpi.network.server.PrPiServer;
import org.apache.log4j.Logger;

public class HostProject extends AnAction {
    private static final Logger logger = Logger.getLogger(HostProject.class);

    private Project project = null;
    private PrPiProjectComponent projectComponent = null;

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        logger.trace("HostProject actionPerformed called");
        try {
            project = anActionEvent.getProject();
            projectComponent = project.getComponent(PrPiProjectComponent.class);
            if (projectComponent.isClient()) {
                Messages.showWarningDialog(project, "You are already a client of this remote project, you can't hosting this project!", "PrPi Warning - Host Is Not Allow");
            } else if (projectComponent.isHosting()) {
                Messages.showWarningDialog(project, "You already host this project!", "PrPi Warning - Project Already Hosted");
            } else {
                // TODO : add window for choosing port
                launchServer(PrPiServer.DEFAULT_PORT);
                logger.trace("Project hosted - Server start");
            }
        } catch (Exception ex) {
            logger.error("Error launching server", ex);
            Messages.showErrorDialog(anActionEvent.getProject(), ex.getMessage(), "Error Starting Server Thread");
        }
        logger.trace("HostProject actionPerformed end");
    }

    private void launchServer(int port) {
        PrPiServer server = new PrPiServer(port, project);
        server.start();
        projectComponent.setServerThread(server);
    }

    @Override
    public void update(AnActionEvent anActionEvent) {
        super.update(anActionEvent);
        anActionEvent.getPresentation().setEnabled(!anActionEvent.getProject().getComponent(PrPiProjectComponent.class).isHosting());
    }
}
