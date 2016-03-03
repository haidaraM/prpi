package com.prpi.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.prpi.network.PrPiServerThread;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.helpers.Loader;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class HostProject extends AnAction {
    private static final Logger logger = Logger.getLogger(HostProject.class);

    private Thread serverThread;
    private static final int DEFAULT_PORT = 4211;

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

    private void launchServerInThread(int port) throws IOException {
        serverThread = new PrPiServerThread(port);
        serverThread.start();
    }
}
