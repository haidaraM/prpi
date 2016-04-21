package com.prpi.network.communication;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

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
     * @return
     */
    public T getContent() {
        logger.debug("Get Content of Message, content type : " + contentType + " / Content : " + content);

        // Used to read directly in the json the type of the final cast of the object
        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(json).getAsJsonObject();
        try {
            Class type = Class.forName(jsonObject.get("contentType").getAsString());
            return (T) gson.fromJson(content, type);
        } catch (ClassNotFoundException e) {
            logger.debug(e);
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
