package com.prpi.network.communication;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

class Message<T> {

    /**
     * The message (String, int, Object ...)
     */
    private T content;

    public Message(T obj) {
        this.content = obj;
    }

    public T getContent() {
        return content;
    }

    private static final Gson gson = new Gson();

    protected String toJson() {
        return gson.toJson(this);
    }

    protected static Message jsonToMessage(String json) throws JsonSyntaxException {
        return gson.fromJson(json, Message.class);
    }
}
