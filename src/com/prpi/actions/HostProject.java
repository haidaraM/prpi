package com.prpi.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import com.prpi.PrPiApplicationComponent;
import com.prpi.network.PrPiServer;
import org.apache.log4j.Logger;

public class HostProject extends AnAction {
    private static final Logger logger = Logger.getLogger(HostProject.class);

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        logger.trace("HostProject actionPerformed called");
        try {
            PrPiApplicationComponent appPrPi = PrPiApplicationComponent.getPrPiAppComp(anActionEvent.getProject());
            if (appPrPi.isClient()) {
                Messages.showWarningDialog(anActionEvent.getProject(), "You are already a client of this remote project, you can't hosting this project!", "PrPi Warning - Host Is Not Allow");
            } else if (appPrPi.isHosting()) {
                Messages.showWarningDialog(anActionEvent.getProject(), "You already host this project!", "PrPi Warning - Project Already Hosted");
            } else {
                PrPiServer server = new PrPiServer(PrPiServer.DEFAULT_PORT, anActionEvent.getProject());
                server.start();
                appPrPi.setServerThread(server);
                logger.debug("Project hosted - Server start");
            }
        } catch (Exception ex) {
            logger.error("Error launching server", ex);
            Messages.showErrorDialog(anActionEvent.getProject(), ex.getMessage(), "Error Starting Server Thread");
        }
        logger.trace("HostProject actionPerformed end");
    }

    @Override
    public void update(AnActionEvent anActionEvent) {
        super.update(anActionEvent);
        anActionEvent.getPresentation().setEnabled(!PrPiApplicationComponent.getPrPiAppComp(anActionEvent.getProject()).isHosting());
    }
}
