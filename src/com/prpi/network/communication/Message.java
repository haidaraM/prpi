package com.prpi.network.communication;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class Message<T> extends Transaction {

    /**
     * The message (String, int, Object ...)
     */
    private T content;

    public Message(T obj, TransactionType transactionType) {
        super(Message.class, transactionType);
        this.content = obj;
        this.json = gson.toJson(this);
    }

    public T getContent() {
        return content;
    }

    @Override
    public String getString(int offset, int length) {
        return this.json.substring(offset, offset + length);
    }

    @Override
    public int getLength() {
        return this.json.length();
    }
}
