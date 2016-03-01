package com.prpi.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.prpi.network.PrPiServer;

import java.io.IOException;

public class HostProject extends AnAction {
    private PrPiServer server;
    private static final int DEFAULT_PORT = 4011;

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        Project project = anActionEvent.getProject();

        try {
            launchServerInThread(DEFAULT_PORT);
            Messages.showMessageDialog(project, "Connection opened on port.\nCollaboration is ready!", "Project hosted", Messages.getInformationIcon());
        } catch (IOException ex) {
            Messages.showErrorDialog(project, ex.getMessage(), "Error creating server");
        }
    }

    private void launchServerInThread(int port) throws IOException {
        server = new PrPiServer(port);
    }
}
