package com.prpi.network.communication;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.prpi.network.PrPiChannelInitializer;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class NetworkTransactionFactory {

    private static final Logger logger = Logger.getLogger(NetworkTransactionFactory.class);

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
     * The builder to send a message on network
     * @param message The message object
     * @param transactionType The transaction type
     * @return a list of all NetworkTransaction to send that represent your original message object given
     */
    public static List<NetworkTransaction> build(@NotNull Transaction message, @NotNull PrPiTransaction transactionType) {

        // The limit of the message length
        final int maxMessageLength = PrPiChannelInitializer.MAX_FRAME_LENGTH
                - new NetworkTransaction(Long.toString(Long.MAX_VALUE), transactionType, Integer.MAX_VALUE, Integer.MAX_VALUE, "").toJson().length()
                - 100; // Increase if needed this random value in case of out of frame length

        // The result
        LinkedList<NetworkTransaction> result = new LinkedList<>();

        // The string length
        int messageLengthLeft = message.getLength();

        // Get the total number of message to buildsssss
        int nbMessage = messageLengthLeft / maxMessageLength;
        if (messageLengthLeft % maxMessageLength > 0) {
            nbMessage++;
        }

        // The counter to get message ID to each one
        int messageID = 0;

        // The ID of the transaction
        String transactionID = NetworkTransactionFactory.getNextTransactionID();

        // The offset in string
        int offset = 0;

        while (messageLengthLeft > maxMessageLength) {
            result.add(new NetworkTransaction(transactionID, transactionType, nbMessage, messageID, message.getString(offset, maxMessageLength)));
            messageID++;
            offset += maxMessageLength;
            messageLengthLeft -= maxMessageLength;
        }
        if (messageLengthLeft > 0) {
            result.add(new NetworkTransaction(transactionID, transactionType, nbMessage, messageID, message.getString(offset, messageLengthLeft)));
        }

        return result;
    }

    private static final Gson gson = new Gson();

    protected static NetworkTransaction jsonToNetworkMessage(@NotNull String json) throws JsonSyntaxException {
        return gson.fromJson(json, NetworkTransaction.class);
    }
}
