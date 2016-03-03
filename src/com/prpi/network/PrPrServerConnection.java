package com.prpi.network;

import java.io.*;
import java.net.Socket;


public class PrPrServerConnection extends Thread {

    /**
     * Connection socket handled
     */
    private Socket clientSocket = null;

    public PrPrServerConnection(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try (
                ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream())
        ) {
            String inputLine, outputLine;

            while (!isInterrupted() && !clientSocket.isClosed()) {
                try {
                    Object msg = in.readObject();
                    System.out.println("reçu : " + (String)msg);
                } catch (Exception ignored){

                }

            }

            clientSocket.close();
        } catch (IOException ioe) {
            System.err.println("Error: " + ioe.getMessage());
        }
    }
}
