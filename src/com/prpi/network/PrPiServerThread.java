package com.prpi.network;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

public class PrPiServerThread extends Thread {
    private PrpiServer server;

    public PrPiServerThread(int port) throws Exception {
        super();
        server = new PrpiServer(port);
    }

    @Override
    public void run() {
        try {
            server.run();
        } catch (Exception e) {
           e.printStackTrace();
        }
    }
}
