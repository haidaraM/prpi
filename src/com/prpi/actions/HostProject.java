package com.prpi.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.prpi.network.PrpiServer;
import org.apache.log4j.Logger;

import java.io.IOException;

public class HostProject extends AnAction {
    private static final Logger logger = Logger.getLogger(HostProject.class);

    private Thread serverThread;

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        logger.debug("HostProject actionPerformed called");

        Project project = anActionEvent.getProject();
        try {
            launchServerInThread(DEFAULT_PORT);
        } catch (IOException ex) {
            logger.error("Error launching server", ex);
            Messages.showErrorDialog(project, ex.getMessage(), "Error Starting Server Thread");
        }
        logger.debug("HostProject actionPerformed end");
    }

    private void launchServerInThread(int port) throws Exception {
        serverThread = new PrpiServer(port);
        serverThread.start();
    }
}
