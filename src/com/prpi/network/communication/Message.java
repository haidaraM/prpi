package com.prpi.network.communication;

import com.prpi.network.server.PrPiServer;
import org.jetbrains.annotations.NotNull;

public class Message {

    /**
     * Version protocol used in this message
     */
    protected String version;

    /**
     * The class of this object to dynamic cast with children
     */
    private Class messageType;

    /**
     * The message (String, int, Object ...)
     */
    protected String message;

    /**
     * The transaction type of this message
     */
    protected PrPiTransaction transactionType;

    /**
     * The ID of the transaction
     */
    protected String transactionID;

    /**
     * The number of this message in this transaction ID (first = 0, second = 1, ...)
     */
    protected int messageID;

    /**
     * The total number of messages in this transaction
     */
    protected int nbMessage;

    protected <T> Message(@NotNull String transactionID, @NotNull PrPiTransaction transactionType, int nbMessage, int messageID, @NotNull Class<T> objectType, @NotNull String message) {
        this.version = PrPiServer.PROTOCOL_PRPI_VERSION;
        this.message = message;
        this.transactionType = transactionType;
        this.transactionID = transactionID;
        this.messageID = messageID;
        this.nbMessage = nbMessage;
        this.messageType = objectType;
    }
}
