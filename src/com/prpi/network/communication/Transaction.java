package com.prpi.network.communication;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

abstract class Transaction {

    private String objectTypeInTransaction;

    /**
     * To store the json result (need to be update when an attribut is changed)
     */
    protected transient String json;

    /**
     * The transaction type of this message
     */
    protected PrPiTransaction transactionType;

    Transaction(Class type, PrPiTransaction transactionType) {
        this.objectTypeInTransaction = type.getCanonicalName();
        this.transactionType = transactionType;
    }

    public String getString(int offset, int length) {
        return json.substring(offset, offset + length);
    }

    public int getLength() {
        return json.length();
    }

    /**
     * The json builder
     */
    protected transient static final Gson gson = new Gson();

    public static Transaction jsonToTransaction(String content) throws ClassNotFoundException {
        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(content).getAsJsonObject();

        return (Transaction) gson.fromJson(content, Class.forName(jsonObject.get("objectTypeInTransaction").getAsString()));
    }
}
