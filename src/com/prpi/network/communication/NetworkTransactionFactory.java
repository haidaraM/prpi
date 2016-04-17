package com.prpi.network.communication;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.psi.PsiManager;
import com.prpi.network.PrPiChannelInitializer;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
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
    public static <T extends Message> List<NetworkTransaction> build(@NotNull T message, @NotNull PrPiTransaction transactionType) {

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

    private static String getPathToFileInProjectRoot(Path pathToFile, Path projectBasePath) {
        if (pathToFile.toString().startsWith(projectBasePath.toString()))
        {
            return pathToFile.toString().substring(projectBasePath.toString().length());
        }

        return pathToFile.toString();
    }

    /**
     * Create the NetworkTransactions of a file, described by its path.
     * @param pathToFile
     * @param projectBasePath
     * @return
     */
    public static List<NetworkTransaction> create(Path pathToFile, Path projectBasePath, @NotNull PrPiTransaction transactionType) {
        if (!Files.isReadable(pathToFile) || Files.isDirectory(pathToFile)) {
            return null;
        }

        String pathInProject = getPathToFileInProjectRoot(pathToFile, projectBasePath);
        String fileName = pathToFile.getFileName().toString();
        int fileSize = PrPiMessageFile.getFileSize(pathToFile);
        logger.debug("Size of file " + pathToFile + " is " + fileSize);

        try {
            // Read all the bytes and Create the File object that will be send.
            byte[] bytes = Files.readAllBytes(pathToFile.toAbsolutePath());
            File fileToSend = new File(fileName, pathInProject, fileSize, Base64.getEncoder().encodeToString(bytes));
            return build(fileToSend, transactionType);
        } catch (IOException e) {
            logger.error("IO exception when reading file", e);
        }

        return null;
    }

    private static final Gson gson = new Gson();

    protected static NetworkTransaction jsonToNetworkMessage(@NotNull String json) throws JsonSyntaxException {
        return gson.fromJson(json, NetworkTransaction.class);
    }
}
