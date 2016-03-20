package com.prpi.network;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class PrPiMessage<T> {

    protected String version;
    protected T message;
    protected boolean closeConnection;
    protected boolean testConnection;

    private static final Gson gson = new Gson();

    public PrPiMessage() {
        this.version = PrPiServer.PROTOCOL_PRPI_VERSION;
        this.message = null;
        this.closeConnection = false;
    }

    public PrPiMessage(T message) {
        this();
        this.message = message;
    }

    public PrPiMessage(boolean close) {
        this();
        this.closeConnection = close;
    }

    public PrPiMessage(T message, boolean close) {
        this(message);
        this.closeConnection = close;
    }

    public String getVersion() {
        return version;
    }

    public T getMessage() {
        return message;
    }

    public void setMessage(T message) {
        this.message = message;
    }

    public boolean isCloseConnection() {
        return closeConnection;
    }

    public void setCloseConnection(boolean closeConnection) {
        this.closeConnection = closeConnection;
    }

    public static PrPiMessage jsonToPrPiMessage(String json) throws JsonSyntaxException{
        return gson.fromJson(json, PrPiMessage.class);
    }

    public String toJson() {
        String json = gson.toJson(this);
        return json + "\n";
    }
}
