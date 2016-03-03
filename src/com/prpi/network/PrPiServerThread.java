package com.prpi.network;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.prpi.networkv2.PrpiServer;

import java.io.IOException;

public class PrPiServerThread extends Thread {
    private PrpiServer server;

    public PrPiServerThread(int port) throws Exception {
        super();
        PrpiServer server = new PrpiServer(port);
    }

    @Override
    public void run() {
        try {
            server.run();
        } catch (Exception e) {
            Messages.showErrorDialog((Project) null, e.getMessage(),
                    "Server Error While Listening for Connections");
        }
    }
}
