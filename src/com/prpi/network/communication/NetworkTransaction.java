package com.prpi.network.communication;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.jetbrains.annotations.NotNull;

public class NetworkTransaction {

    /**
     * The ID of the transaction
     */
    private String networkTransactionID;

    /**
     * The number of this message in this transaction ID (first = 0, second = 1, ...)
     */
    private int messageID;

    /**
     * The total number of messages in this transaction
     */
    private int nbMessage;

    /**
     * The content of this NetworkTransaction
     */
    private String content;

    NetworkTransaction(@NotNull String networkTransactionID, int nbMessage, int messageID, @NotNull String content) {
        this.content = content;
        this.networkTransactionID = networkTransactionID;
        this.messageID = messageID;
        this.nbMessage = nbMessage;
    }

    private static final Gson gson = new Gson();

    String toJson() {
        return gson.toJson(this);
    }

    protected static NetworkTransaction jsonToNetworkMessage(String json) throws JsonSyntaxException {
        return gson.fromJson(json, NetworkTransaction.class);
    }

    protected String getNetworkTransactionID() {
        return networkTransactionID;
    }

    protected int getMessageID() {
        return messageID;
    }

    protected int getNbMessage() {
        return nbMessage;
    }

    protected String getContent() {
        return content;
    }

    public boolean isComposedMessage() {
        return nbMessage > 1;
    }
}
