package com.prpi.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by pierre on 03/03/16.
 */
public class PrPrServerConnection extends Thread {

    /** Connection socket handled */
    private Socket clientSocket = null;

    public PrPrServerConnection(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try (
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        ) {
            String inputLine, outputLine;

            while ((inputLine = in.readLine()) != null) {
                System.err.println("Message received: " + inputLine);

                if (inputLine.contains("exit")) // TODO: Faire la fin de communication du protocole
                {
                    break;
                }
            }

            clientSocket.close();
        } catch (IOException ioe) {
            System.err.println("Error: " + ioe.getMessage());
        }
    }
}
