package com.prpi.network;

import javax.net.ssl.SSLServerSocketFactory;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;

public class PrPiServer {
    private ServerSocket serverSocket;

    public PrPiServer(int port) throws IOException {
        serverSocket = SSLServerSocketFactory.getDefault().createServerSocket(port);
    }

    public void startListening() throws IOException {
        System.out.println("Server is listening on port " + serverSocket.getLocalPort() + " ip : " + Inet4Address.getLocalHost().getHostAddress());

        while (true) {
            Socket incomingConnection = serverSocket.accept();
            System.out.println("Connection accepted !");

            PrPrServerConnection serverConnection = new PrPrServerConnection(incomingConnection);
            serverConnection.start();

        }
    }

}
