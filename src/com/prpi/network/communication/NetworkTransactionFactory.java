package com.prpi.network.communication;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.prpi.network.PrPiChannelInitializer;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class NetworkTransactionFactory {

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
     * @return a list of all NetworkTransaction to send that represent your original message object given
     */
    public static List<NetworkTransaction> build(@NotNull Message message, @NotNull PrPiTransaction transactionType) {

        // The limit of the message length
        final int maxMessageLength = PrPiChannelInitializer.MAX_FRAME_LENGTH
                - new NetworkTransaction(Long.toString(Long.MAX_VALUE), transactionType, Integer.MAX_VALUE, Integer.MAX_VALUE, "").toJson().length()
                - 100; // Increase if needed this random value in case of out of frame length

        // The message to send in String
        StringBuilder buffer = new StringBuilder(message.toJson());

        // The result
        LinkedList<NetworkTransaction> result = new LinkedList<>();

        // Get the total number of message to buildsssss
        int nbMessage = buffer.length() / maxMessageLength;
        if (buffer.length() % maxMessageLength > 0) {
            nbMessage++;
        }

        // The counter to get message ID to each one
        int messageID = 0;

        // The ID of the transaction
        String transactionID = NetworkTransactionFactory.getNextTransactionID();

        while (buffer.length() > maxMessageLength) {
            result.add(new NetworkTransaction(transactionID, transactionType, nbMessage, messageID, buffer.delete(0, maxMessageLength).toString()));
            messageID++;
        }
        if (buffer.length() > 0) {
            result.add(new NetworkTransaction(transactionID, transactionType, nbMessage, messageID, buffer.delete(0, buffer.length()).toString()));
        }

        return result;
    }

    public static List<NetworkTransaction> build(@NotNull File file, @NotNull PrPiTransaction transactionType) {

        // The limit of the message length
        final int maxMessageLength = PrPiChannelInitializer.MAX_FRAME_LENGTH
                - new NetworkTransaction(Long.toString(Long.MAX_VALUE), transactionType, Integer.MAX_VALUE, Integer.MAX_VALUE, "").toJson().length()
                - 100; // Increase if needed this random value in case of out of frame length

        // The result
        LinkedList<NetworkTransaction> result = new LinkedList<>();

        // TODO read step by step the file and make new NetworkTransaction : read the PrPiMessageFileFactory and PrPiMessageFile

        return result;
    }

    private static final Gson gson = new Gson();

    protected String toJson() {
        return gson.toJson(this);
    }

    protected static NetworkTransaction jsonToNetworkMessage(@NotNull String json) throws JsonSyntaxException {
        return gson.fromJson(json, NetworkTransaction.class);
    }
}
