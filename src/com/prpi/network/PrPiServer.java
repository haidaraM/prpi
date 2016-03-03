package com.prpi.network;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import javax.net.ssl.SSLServerSocketFactory;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class PrPiServer {
    private static final Logger logger = LogManager.getLogger(PrPiServer.class);

    private ServerSocket serverSocket;
    private int port;

    public PrPiServer(int port) throws IOException {
        this.port = port;
        serverSocket = SSLServerSocketFactory.getDefault().createServerSocket(port);
    }

    public void startListening() throws IOException {
        logger.trace("PrPi server starts listening on port " + port);
        while (true) {
            Socket incomingConnection = serverSocket.accept();

            PrPrServerConnection serverConnection = new PrPrServerConnection(incomingConnection);
            serverConnection.start();

            System.out.println("Connection accepted !");
        }
    }

}
