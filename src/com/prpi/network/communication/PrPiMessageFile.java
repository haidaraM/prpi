package com.prpi.network.communication;

import com.prpi.network.PrPiChannelInitializer;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class PrPiMessageFile extends PrPiMessage<String> {

    protected String fileName;
    protected String pathInProject;
    protected int fileSize;

    /**
     * When a file is greater than this size (in bytes), the file is cut in multiple messages (when use create method)
     * or throw an exception (when use constructor)
     */
    protected static final int MAX_SIZE_PER_MESSAGE = PrPiChannelInitializer.MAX_FRAME_LENGTH - 100000; // TODO 20000 ??? pq? 60000?

    private static final Logger logger = Logger.getLogger(PrPiMessageFile.class);

    /**
     * Create a specific PrPiMessageFile
     * Used to send a big file, cut in multiple PrPiMessageFile
     * @param transactionID
     * @param nbMessage
     * @param messageID
     * @param fileName
     * @param pathInProject
     * @param fileSize
     * @param fileDataEncodedBase64
     */
    protected PrPiMessageFile(@NotNull String transactionID,
                            int nbMessage,
                            int messageID,
                            @NotNull String fileName,
                            @NotNull String pathInProject,
                            int fileSize,
                            @NotNull String fileDataEncodedBase64) {
        super(transactionID, PrPiTransaction.FILE_TRANSFERT, nbMessage, messageID);
        this.fileName = fileName;
        this.pathInProject = pathInProject;
        this.fileSize = fileSize;
        this.message = fileDataEncodedBase64;
    }



    public void writeFile(Path projectBasePath) throws IOException {
        Path fileDir = createDirectory(projectBasePath);
        Path filePath = Paths.get(fileDir.toString(), fileName);

        try (FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
            writeFileTo(fos);
            logger.debug("Wrote " + filePath);
        }
    }

    public static void writeFiles(Path projectBasePath, List<PrPiMessageFile> messages) {
        PrPiMessageFile firstMessage = messages.get(0);
        Path fileDir = firstMessage.createDirectory(projectBasePath);
        Path filePath = Paths.get(fileDir.toString(), firstMessage.fileName);
        try {
            try (FileOutputStream fileOutputStream = new FileOutputStream(filePath.toFile())) {
                for (int i = 0; i < firstMessage.nbMessage; i++) {
                    PrPiMessageFile message = messages.get(i);
                    message.writeFileTo(fileOutputStream);
                    logger.debug(String.format("Wrote part of %s (%d/%d)", filePath, i, firstMessage.nbMessage));
                }
                logger.debug("Completely wrote " + filePath);
            }
        } catch (IOException e) {
            logger.error("Impossible to write the new file in this path " + filePath.toString(), e);
        }
    }

    /**
     * Creates the directory of a message, with a project base path.
     * @param projectDir The absolute path to the project directory
     * @return The absolute path of the file directory
     */
    private Path createDirectory(Path projectDir) {
        Path fileDir = Paths.get(projectDir.toString() + pathInProject);
        Path parentDirectory = fileDir.getParent();
        try {
            Files.createDirectories(parentDirectory);
            logger.debug("Created directory " + fileDir);
        } catch (IOException e) {
            logger.error("Could not create directory " + fileDir, e);
        }

        return parentDirectory;
    }

    /**
     * Writes the message content to the given output stream
     * @param os Where to write the message content
     */
    private void writeFileTo(OutputStream os) {
        byte[] fileData = decodeFileData(message);
        try {
            os.write(fileData);
            logger.debug("Wrote file of size : " + fileSize);
        } catch (IOException e) {
            logger.error("Could not write file", e);
        }
    }

    protected static int getFileSize(Path pathToFile) {
        long size;
        try {
            size = Files.size(pathToFile);
            if (size > Integer.MAX_VALUE)
                return -1;
            return (int) size;
        } catch (IOException e) {
            logger.error("Could not estimate file size", e);
            return -1;
        }
    }

    // TODO static?
    protected static String encodeFileData(byte[] fileData) {
        return Base64.getEncoder().encodeToString(fileData);
    }

    // TODO static?
    protected static byte[] decodeFileData(String fileDataEncodedBase64) {
        return Base64.getDecoder().decode(fileDataEncodedBase64);
    }

    @Override
    public String toString() {
        return "PrPiMessageFile{" +
                "fileName='" + fileName + '\'' +
                ", pathInProject='" + pathInProject + '\'' +
                ", fileSize=" + fileSize +
                "} " + super.toString();
    }
}
