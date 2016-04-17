package com.prpi.network.communication;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

class Message<T> extends Transaction {

    /**
     * The message (String, int, Object ...)
     */
    private T content;

    /**
     * To store the json result (need to be update when an attribut is changed)
     */
    private transient String json;

    /**
     * The json builder
     */
    private transient static final Gson gson = new Gson();

    public Message(T obj) {
        super(Message.class);
        this.content = obj;
        this.json = gson.toJson(this);
    }

    public T getContent() {
        return content;
    }

    @Override
    public String getString(int offset, int length) {
        return this.json.substring(offset, length);
    }

    @Override
    public int getLength() {
        return this.json.length();
    }

    /*
    protected static Message jsonToMessage(String json) throws JsonSyntaxException {
        return gson.fromJson(json, Message.class);
    }
    */
}
