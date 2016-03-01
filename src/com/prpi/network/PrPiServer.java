package com.prpi.network;

import javax.net.ssl.SSLServerSocketFactory;
import java.io.IOException;
import java.net.ServerSocket;

public class PrPiServer {
    private int port;
    private ServerSocket serverSocket;

    public PrPiServer(int port) throws IOException {
        this.port = port;
        serverSocket = SSLServerSocketFactory.getDefault().createServerSocket(port);
    }

}
