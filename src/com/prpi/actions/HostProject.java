package com.prpi.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.prpi.PrPiApplicationComponent;
import com.prpi.network.PrPiServer;
import org.apache.log4j.Logger;

public class HostProject extends AnAction {
    private static final Logger logger = Logger.getLogger(HostProject.class);

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        logger.trace("HostProject actionPerformed called");

        Project project = anActionEvent.getProject();
        if (project == null) {
            logger.error("Error: no project found");
            return;
        }
        try {
            PrPiApplicationComponent component = project.getComponent(PrPiApplicationComponent.class);
            if (component != null) {
                if (component.isClient()) {
                    Messages.showWarningDialog(project, "You are already a client of this remote project, you can't hosting this project!", "PrPi Warning - Host Is Not Allow");
                } else if (component.isHosting()) {
                    Messages.showWarningDialog(project, "You already host this project!", "PrPi Warning - Project Already Hosted");
                } else {
                    PrPiServer server = new PrPiServer(PrPiServer.DEFAULT_PORT);
                    server.start();
                    component.setServerThread(server);
                }
            } else {
                logger.error("Error: no component found");
            }
        } catch (Exception ex) {
            logger.error("Error launching server", ex);
            Messages.showErrorDialog(project, ex.getMessage(), "Error Starting Server Thread");
        }
        logger.trace("HostProject actionPerformed end");
    }
}
