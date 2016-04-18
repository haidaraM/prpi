package com.prpi.network.communication;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.prpi.network.PrPiChannelInitializer;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
     * Build all NetworkTransaction corresponding to the Transaction given and send them to the receiver
     * @param message the message to send (Transaction)
     * @param transactionType the type of Transaction
     * @param receiver the channel receiver of this message(s)
     * @return all ChannelFuture created to send messages
     */
    public static List<ChannelFuture> buildAndSend(@NotNull Transaction message, @NotNull Transaction.TransactionType transactionType, @NotNull final ChannelHandlerContext receiver) {

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

        // List of ChannelFuture to return
        List<ChannelFuture> channelFutureToReturn = new LinkedList<>();

        while (messageLengthLeft > maxMessageLength) {

            // Create the NetworkTransaction, part of the final Message
            NetworkTransaction transaction = new NetworkTransaction(transactionID, transactionType, nbMessage, messageID, message.getString(offset, maxMessageLength));

            // Send this NetworkTransaction and get the ChannelFuture created
            channelFutureToReturn.add(receiver.writeAndFlush(transaction.toJson()));

            // Update, ID, offset and message length left to send
            messageID++;
            offset += maxMessageLength;
            messageLengthLeft -= maxMessageLength;
        }

        if (messageLengthLeft > 0) {
            // Last NetworkTransaction
            NetworkTransaction transaction = new NetworkTransaction(transactionID, transactionType, nbMessage, messageID, message.getString(offset, messageLengthLeft));

            // Send and store the ChannelFuture
            channelFutureToReturn.add(receiver.writeAndFlush(transaction.toJson()));
        }
        return channelFutureToReturn;
    }

    /**
     * Build all NetworkTransaction corresponding to the File given and send them to the receiver
     * @param file the path to the file to send
     * @param projectRoot the path to the project root
     * @param transactionType the transaction type
     * @param receiver the channel receiver of all NetworkTransaction sent
     * @return all ChannelFuture created to send messages
     */
    public static List<ChannelFuture> buildAndSend(Path file, Path projectRoot, @NotNull Transaction.TransactionType transactionType, @NotNull final ChannelHandlerContext receiver) {

        // List of ChannelFuture to return
        List<ChannelFuture> channelFutureToReturn = new LinkedList<>();

        if (!Files.isReadable(file)) {
            logger.error("The file to build and send is not readable or reachable : " + file.toString());
            return channelFutureToReturn;
        }

        // If its a directory, need to build each file in this directory
        if (Files.isDirectory(file)) {

            // Get the directory
            java.io.File directory = file.toFile();

            // Get all files in this directory
            java.io.File[] subFiles = getFilesToSend(directory);

            // ForEach files, build and send
            for (java.io.File subFile : subFiles) {

                // Get all ChannelFuture made to send all NetworkMessage
                channelFutureToReturn.addAll(buildAndSend(subFile.toPath(), projectRoot, transactionType, receiver));
            }

        // Its a file
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

                    // Get all ChannelFuture made to send all NetworkMessage
                    channelFutureToReturn.addAll(buildAndSend(fileContent, transactionType, receiver));

                    // Update index and fileData array size
                    fileContentNumber++;
                    fileData = new byte[Math.min(bufferSize, fileSize)];
                }

            } catch (FileNotFoundException e) {
                logger.error("File not found error, parts are maybe already sent ...", e);
            } catch (IOException e) {
                logger.error("IO exception when reading file, parts are maybe already sent ...", e);
            }
        }
        return channelFutureToReturn;
    }

    /**
     * The Json builder
     */
    private static final Gson gson = new Gson();

    /**
     * Try to convert a string representing a json of an NetworkTransaction object
     * @param json the string json to convert
     * @return the NetworkTransaction object
     * @throws JsonSyntaxException in case of not good json of the NetworkTransaction object
     */
    protected static NetworkTransaction jsonToNetworkMessage(@NotNull String json) throws JsonSyntaxException {
        return gson.fromJson(json, NetworkTransaction.class);
    }

    /**
     * Get all sub file contain in the directory
     * @param directory the directory to scan
     * @return all files contain in the directory given
     */
    private static java.io.File[] getFilesToSend(java.io.File directory) {
        return directory.listFiles((dir, name) -> !name.equals(".idea"));
    }
}
