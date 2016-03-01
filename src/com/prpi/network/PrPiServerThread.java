package com.prpi.network;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

import java.io.IOException;

public class PrPiServerThread extends Thread {
    private PrPiServer server;

    public PrPiServerThread(int port) throws IOException {
        super();
        server = new PrPiServer(port);
    }

    @Override
    public void run() {
        try {
            server.startListening();
        } catch (IOException ex) {
            Messages.showErrorDialog((Project)null, ex.getMessage(), "Server Error While Listening for Connections");
        }
    }
}
