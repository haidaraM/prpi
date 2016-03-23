package com.prpi.network;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicLong;

public class PrPiMessage<T> {

    /**
     * Version protocol used in this message
     */
    protected String version;

    /**
     * The message (String, int, Object ...)
     */
    protected T message;

    /**
     * The transaction type of this message
     */
    protected PrPiTransaction transaction;

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

    private static AtomicLong generatorTransactionID = new AtomicLong();
    private static final Gson gson = new Gson();
    private static final Logger logger = Logger.getLogger(PrPiMessage.class);

    public PrPiMessage() {
        this.version = PrPiServer.PROTOCOL_PRPI_VERSION;
        this.message = null;
        this.transaction = PrPiTransaction.SIMPLE_MESSAGE;
        this.transactionID = PrPiMessage.getNextID();
        this.messageID = 0;
        this.nbMessage = 1;
    }

    public PrPiMessage(@Nullable T message) {
        this();
        this.message = message;
    }

    public PrPiMessage(@NotNull PrPiTransaction transactionType) {
        this();
        this.transaction = transactionType;
    }

    public PrPiMessage(@Nullable T message, @NotNull PrPiTransaction transactionType) {
        this(message);
        this.transaction = transactionType;
    }

    public String toJson() {
        String json = gson.toJson(this);
        return json + "\n";
    }

    public static PrPiMessage jsonToPrPiMessage(@NotNull String json) throws JsonSyntaxException{
        return gson.fromJson(json, PrPiMessage.class);
    }

    private static @NotNull String getNextID()
    {
        return String.valueOf(generatorTransactionID.getAndIncrement());
    }

    // Getter & Setter *************************************************************************************************

    public String getVersion() {
        return version;
    }

    public T getMessage() {
        return message;
    }

    public void setMessage(T message) {
        this.message = message;
    }

    public String getTransactionID() {
        return transactionID;
    }

    public PrPiTransaction getTransaction() {
        return transaction;
    }

    public void setTransaction(PrPiTransaction transaction) {
        this.transaction = transaction;
    }

    public int getNbMessage() {
        return nbMessage;
    }

    public void setNbMessage(int nbMessage) {
        this.nbMessage = nbMessage;
    }

    public int getMessageID() {
        return messageID;
    }

    public void setMessageID(int messageID) {
        this.messageID = messageID;
    }
}
