package com.prpi.network;

import com.prpi.network.client.PrPiClient;
import com.prpi.network.server.PrPiServer;
import org.junit.Before;
import org.junit.Test;

public class FileTransfertTests {

    private PrPiServer server = new PrPiServer();
    private PrPiClient client = new PrPiClient("localhost");

    @Before
    public void setupServerClient() {
        client.connect();
    }

    @Test
    public void transfertSimpleFile() {
        client.initConnection();
        String bigFile;

        server.sendFile(bigFile);
    }
}
