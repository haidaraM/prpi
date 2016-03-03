package com.prpi.networkv2;

public class PrPiClientThread extends Thread {
    private PrPiClient client;

    public PrPiClientThread(String ip, int port) throws java.io.IOException {
        super();
        client = new PrPiClient(ip, port);
    }

    @Override
    public void run() {
        client.startListening();
    }

}
