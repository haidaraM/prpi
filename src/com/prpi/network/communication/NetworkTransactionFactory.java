package com.prpi.network.communication;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.prpi.network.ChannelInitializer;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

public class NetworkTransactionFactory {

    private static final Logger logger = Logger.getLogger(NetworkTransactionFactory.class);

    /**
     * The generator of network transaction ID
     */
    private static AtomicLong generatorNetworkTransactionID = new AtomicLong();

    /**
     * The method to call to get the network transaction ID
     * @return a new unique network transaction ID
     */
    private static @NotNull String getNextNetworkTransactionID() {
        return String.valueOf(generatorNetworkTransactionID.getAndIncrement());
    }

    /**
     * The limit of the message length
     */
    private static final int maxMessageLength = ChannelInitializer.MAX_FRAME_LENGTH
            - new NetworkTransaction(Long.toString(Long.MAX_VALUE), Integer.MAX_VALUE, Integer.MAX_VALUE, "").toJson().length()
            - 100; // Increase if needed this random value in case of out of frame length

    // To get the value of the max length in debug
    static {
        logger.trace("The max length of the content message is : " + NetworkTransactionFactory.maxMessageLength);
    }

    /**
     * The limit of the array size when decomposed a file
     */
    private static final int fileContentBufferSize = 524288;

    /**
     * Build all NetworkTransaction corresponding to the Transaction given and send them to the receiver
     * @param message the message to send (Transaction)
     * @param receiver the channel receiver of this message(s)
     * @return all ChannelFuture created to send messages
     */
    public static List<ChannelFuture> buildAndSend(@NotNull Transaction message, @NotNull final Channel receiver) {

        // The string length
        int messageLengthLeft = message.getLength();

        // Get the total number of message to build
        int nbMessage = messageLengthLeft / maxMessageLength;
        if (messageLengthLeft % maxMessageLength > 0) {
            nbMessage++;
        }

        // The counter to get message ID to each one
        int messageID = 0;

        // The ID of the network transaction
        String netWorkTransactionID = NetworkTransactionFactory.getNextNetworkTransactionID();

        // The offset in string
        int offset = 0;

        // List of ChannelFuture to return
        List<ChannelFuture> channelFutureToReturn = new LinkedList<>();

        while (messageLengthLeft > maxMessageLength) {

            // Create the NetworkTransaction, part of the final Message
            // TODO change message.getString by something to get more performances ??
            NetworkTransaction networkTransaction = new NetworkTransaction(netWorkTransactionID, nbMessage, messageID, message.getString(offset, maxMessageLength));

            // Send this NetworkTransaction and get the ChannelFuture created
            channelFutureToReturn.add(receiver.writeAndFlush(networkTransaction.toJson()));

            // Update, ID, offset and message length left to send
            messageID++;
            offset += maxMessageLength;
            messageLengthLeft -= maxMessageLength;
        }

        if (messageLengthLeft > 0) {
            // Last NetworkTransaction
            NetworkTransaction networkTransaction = new NetworkTransaction(netWorkTransactionID, nbMessage, messageID, message.getString(offset, messageLengthLeft));

            // Send and store the ChannelFuture
            channelFutureToReturn.add(receiver.writeAndFlush(networkTransaction.toJson()));
        }
        return channelFutureToReturn;
    }

    /**
     * Build all NetworkTransaction corresponding to the File given and send them to the receiver
     * @param file the path to the file to send
     * @param projectRoot the path to the project root
     * @param receiver the channel receiver of all NetworkTransaction sent
     * @return all ChannelFuture created to send messages
     */
    public static List<ChannelFuture> buildAndSend(Path file, Path projectRoot, @NotNull final Channel receiver) {

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
            java.io.File[] subFiles = getFilesInDirectory(directory);

            // ForEach files, build and send
            for (java.io.File subFile : subFiles) {

                // Get all ChannelFuture made to send all NetworkMessage
                channelFutureToReturn.addAll(buildAndSend(subFile.toPath(), projectRoot, receiver));
            }

        // Its a file
        } else {

            File fileToSend = new File(file, projectRoot, Transaction.TransactionType.FILE_TRANSFERT);
            buildAndSend(fileToSend, receiver);

            int fileSize = fileToSend.getSize();
            logger.debug("Size of file " + file + " is " + fileSize);

            try (FileInputStream fileInputStream = new FileInputStream(file.toFile()))
            {
                int fileContentNumber = 0;
                byte[] fileData = new byte[Math.min(fileContentBufferSize, fileSize)];
                int dataRead;

                while ((dataRead = fileInputStream.read(fileData)) > 0) {

                    boolean lastContent = (dataRead < fileContentBufferSize);
                    logger.trace("Is last part of " + file + " ? " + (lastContent ? "yes" : "no") + " ( data read is " + dataRead + " / fileContentBufferSize is " + fileContentBufferSize + " )");

                    FileContent fileContent = new FileContent(fileToSend.getId(), fileData, dataRead, fileContentNumber, lastContent, Transaction.TransactionType.FILE_CONTENT);

                    // Get all ChannelFuture made to send all NetworkMessage
                    channelFutureToReturn.addAll(buildAndSend(fileContent, receiver));

                    // Update index and fileData array size
                    fileContentNumber++;
                    fileData = new byte[Math.min(fileContentBufferSize, fileSize)];
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
     * List of all pattern to exclude when fetching project files
     */
    public static final Pattern[] fileToExcludeInProject = new Pattern[]{
        Pattern.compile("^\\.idea$")
    };

    /**
     * Get all sub file contain in the directory
     * @param directory the directory to scan
     * @return all files contain in the directory given
     */
    private static java.io.File[] getFilesInDirectory(java.io.File directory) {
        return directory.listFiles((dir, name) -> {
            for (Pattern pattern: fileToExcludeInProject) {
                if (pattern.matcher(name).find()) {
                    return false;
                }
            }
            return true;
        });
    }

    /*public static int getProjectSize(Path projectRoot) throws IOException {
        return (int) Files.walk(projectRoot).mapToLong(p -> {
            for (Pattern pattern: fileToExcludeInProject) {
                java.io.File file = p.toFile();
                if (pattern.matcher(file.getName()).find()) {
                    return 0;
                }
            }
            return p.toFile().length();
        }).sum();
    }*/

    public static int getFilesCount(Path projectRoot) {
        java.io.File file = projectRoot.toFile();
        java.io.File[] files = file.listFiles();
        int count = 0;
        for (java.io.File f : files) {

            boolean isExcluded = false;
            for (Pattern pattern : fileToExcludeInProject)
                if (pattern.matcher(f.getName()).find()) {
                    isExcluded = true;
                    break;
                }

            if (isExcluded)
                continue;

            if (f.isDirectory())
                count += getFilesCount(f.toPath());
            else
                count++;
        }

        return count;
    }
}
