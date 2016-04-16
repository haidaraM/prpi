package com.prpi.network.communication;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.prpi.network.PrPiChannelInitializer;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class MessageFactory {

    private static final Logger logger = Logger.getLogger(MessageFactory.class);

    /**
     * The JSON builder
     */
    private static final Gson gson = new Gson();

    /**
     * The generator of transaction ID
     */
    private static AtomicLong generatorTransactionID = new AtomicLong();

    /**
     * The method to call to get the transaction ID
     * @return a new unique transaction ID
     */
    private static @NotNull String getNextTransactionID() {
        return String.valueOf(generatorTransactionID.getAndIncrement());
    }

    /**
     * The builder to send a message one the network
     * @param message The message object
     * @param transactionType The transaction type
     * @param <T> The type of the message object
     * @return a list of all Message to send that represent your original message object given
     */
    public static <T> List<Message> build(@NotNull T message, @NotNull PrPiTransaction transactionType) {

        // The limit of the message length
        final int maxMessageLength = PrPiChannelInitializer.MAX_FRAME_LENGTH
                - gson.toJson(new Message(Long.toString(Long.MAX_VALUE), transactionType, Integer.MAX_VALUE, Integer.MAX_VALUE, message.getClass(), "")).length()
                - 100; // Increase if needed this random value in case of out of frame length

        // The message to send in String
        StringBuffer buffer = new StringBuffer(gson.toJson(message));

        // The result
        LinkedList<Message> result = new LinkedList<>();

        // Get the total number of message to buildsssss
        int nbMessage = buffer.length() / maxMessageLength;
        if (buffer.length() % maxMessageLength > 0)
            nbMessage++;

        // The counter to get message ID to each one
        int messageID = 0;

        // The ID of the transaction
        String transactionID = MessageFactory.getNextTransactionID();

        while (buffer.length() > maxMessageLength) {
            result.add(new Message(transactionID, transactionType, nbMessage, messageID, String.class, buffer.delete(0, maxMessageLength).toString()));
        }
        if (buffer.length() > 0) {
            result.add(new Message(transactionID, transactionType, nbMessage, messageID, String.class, buffer.delete(0, buffer.length()).toString()));
        }

        return result;
    }

    public static Message jsonToMessage(@NotNull String json) throws JsonSyntaxException, ClassNotFoundException {
        return gson.fromJson(json, Message.class);
    }
}
