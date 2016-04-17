package com.prpi.network.communication;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.prpi.network.PrPiChannelInitializer;
import io.netty.channel.ChannelHandlerContext;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
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
    public static void buildAndSend(@NotNull Transaction message, @NotNull PrPiTransaction transactionType, @NotNull final ChannelHandlerContext receiver) {

        // The limit of the message length
        final int maxMessageLength = PrPiChannelInitializer.MAX_FRAME_LENGTH
                - new NetworkTransaction(Long.toString(Long.MAX_VALUE), transactionType, Integer.MAX_VALUE, Integer.MAX_VALUE, "").toJson().length()
                - 100; // Increase if needed this random value in case of out of frame length

        // The string length
        int messageLengthLeft = message.getLength();

        // Get the total number of message to build
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
            NetworkTransaction transaction = new NetworkTransaction(transactionID, transactionType, nbMessage, messageID, message.getString(offset, maxMessageLength));
            receiver.writeAndFlush(transaction.toJson());
            messageID++;
            offset += maxMessageLength;
            messageLengthLeft -= maxMessageLength;
        }
        if (messageLengthLeft > 0) {
            NetworkTransaction transaction = new NetworkTransaction(transactionID, transactionType, nbMessage, messageID, message.getString(offset, messageLengthLeft));
            receiver.writeAndFlush(transaction.toJson());
        }
    }

    private static java.io.File[] getFilesToSend(java.io.File directory) {
        return directory.listFiles((dir, name) -> !name.equals(".idea"));
    }

    public static void buildAndSend(Path file, Path projectRoot, @NotNull PrPiTransaction transactionType, @NotNull final ChannelHandlerContext receiver) {
        if (!Files.isReadable(file)) {
            // TODO : Warning/error
            return;
        }

        if (Files.isDirectory(file)) {
            java.io.File directory = file.toFile();
            java.io.File[] subFiles = getFilesToSend(directory);

            for (java.io.File subFile : subFiles) {
                buildAndSend(subFile.toPath(), projectRoot, transactionType, receiver);
            }
        } else {

            File fileToSend = new File(file, projectRoot, transactionType);
            buildAndSend(fileToSend, transactionType, receiver);

            int fileSize = fileToSend.getSize();
            logger.debug("Size of file " + file + " is " + fileSize);

            // The limit of the message length
            final int bufferSize = 65536; // TODO : find best value

            try (FileInputStream fileInputStream = new FileInputStream(file.toFile()))
            {
                int fileContentNumber = 0;
                byte[] fileData = new byte[Math.min(bufferSize, fileSize)];
                int dataRead;

                while ((dataRead = fileInputStream.read(fileData)) > 0) {
                    boolean lastContent = (dataRead < bufferSize);
                    FileContent fileContent = new FileContent(fileToSend.getId(), fileData, dataRead, fileContentNumber, lastContent, transactionType);
                    buildAndSend(fileContent, transactionType, receiver);

                    fileContentNumber++;
                    fileData = new byte[Math.min(bufferSize, fileSize)];
                }

            } catch (FileNotFoundException e) {
                logger.error("File not found error", e);
            } catch (IOException e) {
                logger.error("IO exception when reading file", e);
            }
        }
    }

    private static final Gson gson = new Gson();

    protected static NetworkTransaction jsonToNetworkMessage(@NotNull String json) throws JsonSyntaxException {
        return gson.fromJson(json, NetworkTransaction.class);
    }
}
