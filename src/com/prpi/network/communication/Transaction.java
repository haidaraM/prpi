package com.prpi.network.communication;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

public abstract class Transaction {

    /**
     * All available transaction between client/server
     */
    public enum TransactionType {
        FILE_TRANSFERT,
        FILE_CONTENT,
        SIMPLE_MESSAGE,
        INIT_PROJECT,
        PROJECT_FILES,
        CLOSE
    }

    /**
     * Used to dynamicly cast in the jsonToTransaction conversion (Message, File, FileContent ...)
     */
    private String objectTypeInTransaction;

    /**
     * To store the json result (need to be update when an attribut is changed)
     */
    protected transient String json;

    /**
     * The transaction type of this message
     */
    private TransactionType transactionType;

    Transaction(Class type, TransactionType transactionType) {
        this.objectTypeInTransaction = type.getCanonicalName();
        this.transactionType = transactionType;
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

    /**
     * The json builder
     */
    protected transient static final Gson gson = new Gson();

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
