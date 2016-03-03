package com.prpi.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.prpi.network.PrPiClientThread;
import org.apache.log4j.Logger;

import java.io.IOException;

public class JoinProject extends AnAction {
    private static final Logger logger = Logger.getLogger(JoinProject.class);

    private Thread clientThread;
    private static final int DEFAULT_PORT = 4211;

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        logger.debug("JoinProject actionPerformed begin");
        Project project = anActionEvent.getProject();

        String ipAddress = Messages.showInputDialog(project, "IP ?", "IP", Messages.getQuestionIcon());
        try {
            joinServerInThread(DEFAULT_PORT, ipAddress);
        } catch (IOException ex) {
            logger.error("Connection error", ex);
            Messages.showErrorDialog(project, ex.getMessage(), "Error Starting Server Thread");
        }
        logger.debug("JoinProject actionPerformed end");
    }

    private void joinServerInThread(int port, String ip) throws IOException {
        clientThread = new PrPiClientThread(port, ip);
        clientThread.start();
    }
}
