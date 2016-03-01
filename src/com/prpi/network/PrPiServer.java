package com.prpi.network;

import javax.net.ssl.SSLServerSocketFactory;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class PrPiServer {
    private ServerSocket serverSocket;

    public PrPiServer(int port) throws IOException {
        serverSocket = SSLServerSocketFactory.getDefault().createServerSocket(port);
    }

    public void startListening() throws IOException {
        System.out.println("Server is listening");
        while (true) {
            Socket incomingConnection = serverSocket.accept();
            // TODO create thread handling connection

            System.out.println("Connection accepted !");
        }
    }

}
