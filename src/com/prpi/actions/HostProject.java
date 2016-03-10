package com.prpi.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.prpi.network.PrPiServerThread;
import com.prpi.network.PrpiServer;

public class HostProject extends AnAction {
    private Thread serverThread;

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        System.out.println("HostProject ACTION PERFORMED");
        Project project = anActionEvent.getProject();

        try {
            launchServerInThread(PrpiServer.DEFAULT_PORT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void launchServerInThread(int port) throws Exception {
        serverThread = new PrPiServerThread(port);
        serverThread.start();
    }
}
