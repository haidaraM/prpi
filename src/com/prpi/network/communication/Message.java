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
        this.json = Transaction.gson.toJson(this);
    }

    /**
     * Get the content of the Message
     * @return
     */
    public T getContent() {
        return content;
    }

    @Override
    public String toString() {
        return "Message{" +
                "content=" + content +
                "} " + super.toString();
    }
}
