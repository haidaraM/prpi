package com.prpi.network.communication;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.Expose;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicLong;

public abstract class Transaction {

    /**
     * All available transaction between client/server
     */
    public enum TransactionType {
        FILE_TRANSFERT,
        FILE_CONTENT,
        SIMPLE_MESSAGE,
        INIT_PROJECT,
        HEART_BEAT,
        PROJECT_NAME,
        NUMBER_OF_PROJECT_FILES,
        CLOSE,
        CONNECT,
        ACCEPTED,
        REFUSED
    }

    /**
     * Used to dynamicly cast in the jsonToTransaction conversion (Message, File, FileContent ...)
     */
    protected String objectTypeInTransaction;

    /**
     * To store the json result (need to be update when an attribut is changed)
     */
    @Expose(serialize = false)
    protected String json;

    /**
     * The transaction type of this message
     */
    private TransactionType transactionType;

    /**
     * The ID of this transaction
     */
    private String transactionID;

    /**
     * If true, the receiver need to make a response with the same transaction ID
     */
    private boolean waitingResponse = false;

    /**
     * The generator of transaction ID
     */
    private static AtomicLong generatorTransactionID = new AtomicLong();

    /**
     * The method to call to get the transaction ID
     * @return a new transaction ID
     */
    private static @NotNull String getNextTransactionID() {
        return String.valueOf(generatorTransactionID.getAndIncrement());
    }

    Transaction(Class type, TransactionType transactionType) {
        this.objectTypeInTransaction = type.getCanonicalName();
        this.transactionType = transactionType;
        this.waitingResponse = false;
        this.transactionID = Transaction.getNextTransactionID();
        this.json = Transaction.gson.toJson(this);
    }

    /**
     * Get a String part of the final json of this object
     * @param offset index of the begin string to extract
     * @param length the length of the string exctracted
     * @return the String part of the json
     */
    public String getString(int offset, int length) {
        return json.substring(offset, offset + length);
    }

    /**
     * Get the length of the String representing the json of the object
     * @return the length of the json string
     */
    public int getLength() {
        return json.length();
    }

    /**
     * Get the type of this Transaction
     * @return the type
     */
    public TransactionType getTransactionType() {
        return transactionType;
    }

    public boolean isWaitingResponse() {
        return waitingResponse;
    }

    public void setWaitingResponse(boolean waitingResponse) {
        this.waitingResponse = waitingResponse;
        this.json = Transaction.gson.toJson(this);
    }

    public String getTransactionID() {
        return transactionID;
    }

    public void setTransactionID(String transactionID) {
        this.transactionID = transactionID;
        this.json = Transaction.gson.toJson(this);
    }

    /**
     * The json builder
     */
    protected static final Gson gson = new Gson();

    /**
     * Try to convert a json to a valid Transaction object
     * @param json the json to convert
     * @return the Transaction object in case of succed
     * @throws ClassNotFoundException if the type specified in the Transaction not corresponding to a valid java class
     * @throws JsonSyntaxException if the json no corresponding to a valid transaction class
     */
    public static Transaction jsonToTransaction(String json) throws ClassNotFoundException, JsonSyntaxException {
        // Used to read directly in the json the type of the final cast of the object
        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(json).getAsJsonObject();
        Class type = Class.forName(jsonObject.get("objectTypeInTransaction").getAsString());

        // Create this object and re-cast to the transaction (but its a Message or a File, or a FileContent ...)
        return (Transaction) gson.fromJson(json, type);
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "objectTypeInTransaction='" + objectTypeInTransaction + '\'' +
                ", json='" + json + '\'' +
                ", transactionType=" + transactionType +
                '}';
    }
}
