package com.prpi.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.prpi.networkv2.PrPiServerThread;
import com.prpi.networkv2.PrpiServer;

import java.io.IOException;

public class HostProject extends AnAction {
    private Thread serverThread;
    private static final int DEFAULT_PORT = 4211;

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        System.out.println("ACTION PERFORMED");
        Project project = anActionEvent.getProject();

        try {
            launchServerInThread(DEFAULT_PORT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void launchServerInThread(int port) throws Exception {
        serverThread = new PrPiServerThread(port);
        serverThread.start();
    }
}
