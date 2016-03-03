package com.prpi.network;

import org.apache.log4j.Logger;

import javax.net.ssl.*;
import java.io.*;
import java.net.*;

public class PrPiClient {
    private static final Logger logger = Logger.getLogger(PrPiServer.class);
    private Socket clientSocket;
    private InetSocketAddress serverAddress;

    public PrPiClient(String ip, int port) throws IOException {
        serverAddress = new InetSocketAddress(ip, port);
        clientSocket = new Socket();
    }

    public void startListening() throws IOException {
        clientSocket.connect(serverAddress, 2000);
        logger.trace("Client connected to " + serverAddress);
    }

}
