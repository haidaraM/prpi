package com.prpi.network.communication;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Message<T> extends Transaction {

    private static final Logger logger = Logger.getLogger(Message.class);

    /**
     * The message (String, int, Object ...)
     */
    private String content;

    private String contentType;

    public Message(@NotNull T obj, TransactionType transactionType) {
        super(Message.class, transactionType);
        this.contentType = obj.getClass().getCanonicalName();
        this.content = Transaction.gson.toJson(obj);
        this.json = Transaction.gson.toJson(this);
    }

    /**
     * Get the content of the Message
     * @return the object, or null in case of problem when transform the content in object
     */
    public @Nullable T getContent() {
        logger.trace("Get Content of Message, content type : " + contentType + " / Content : " + content);
        try {
            Class type = Class.forName(contentType);
            return (T) gson.fromJson(content, type);
        } catch (ClassNotFoundException e) {
            logger.error("Problem when change message content (json) in object", e);
        }
        return null;
    }

    @Override
    public String toString() {
        return "Message{" +
                "content=" + content +
                "} " + super.toString();
    }
}
