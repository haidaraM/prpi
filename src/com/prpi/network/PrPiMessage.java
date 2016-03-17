package com.prpi.network;

public class PrPiMessage {

    protected String version;
    protected Object message;
    protected boolean closeConnection;

    public PrPiMessage() {
        this.version = PrPiServer.PROTOCOL_PRPI_VERSION;
        this.message = null;
        this.closeConnection = false;
    }

    public PrPiMessage(Object message) {
        this();
        this.message = message;
    }

    public PrPiMessage(boolean close) {
        this();
        this.closeConnection = close;
    }

    public PrPiMessage(Object message, boolean close) {
        this(message);
        this.closeConnection = close;
    }

    public String getVersion() {
        return version;
    }

    public Object getMessage() {
        return message;
    }

    public void setMessage(Object message) {
        this.message = message;
    }

    public boolean isCloseConnection() {
        return closeConnection;
    }

    public void setCloseConnection(boolean closeConnection) {
        this.closeConnection = closeConnection;
    }
}
