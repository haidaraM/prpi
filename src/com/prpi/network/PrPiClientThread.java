package com.prpi.network;

import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.project.Project;

public class PrPiClientThread extends Thread {
    private PrPiClient client;

    public PrPiClientThread(int port, String ip) throws java.io.IOException {
        super();
        client = new PrPiClient(ip, port);
    }

    @Override
    public void run() {
        try {
            client.startListening();
        } catch (java.io.IOException ex) {
            Messages.showErrorDialog((Project)null, ex.getMessage(), "Client Error While Listening for Connections");
        }
    }

}
