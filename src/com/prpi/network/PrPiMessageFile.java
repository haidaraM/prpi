package com.prpi.network;

import com.google.gson.Gson;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class PrPiMessageFile extends PrPiMessage<String> {

    protected String fileName;
    protected String pathInProject;
    protected long fileSize;
    private static int maxSizeFile = PrPiChannelInitializer.maxFrameLength/2;

    private static final Logger logger = Logger.getLogger(PrPiMessageFile.class);
    private static final Gson gson = new Gson();

    /**
     * Create a simple message to transfert file
     * @param pathToFile the path to the file to transfert
     * @param projectBasePath the path to the root project
     * @throws IOException
     * @throws OutOfMemoryError
     */
    public PrPiMessageFile(Path pathToFile, Path projectBasePath) throws IOException, OutOfMemoryError {
        super();
        this.transaction = PrPiTransaction.FILE_TRANSFERT;
        PrPiMessageFile.isReadableAndIsNotDirectory(pathToFile); // Throws FileNotFoundException
        this.fileName = pathToFile.getFileName().toString();
        this.pathInProject = PrPiMessageFile.getPathToFileInProjectRoot(pathToFile, projectBasePath); // Throws FileNotFoundException
        // By security, we need to add the json informations with data,
        // so in reality they are more than this.fileSize byte in the final message
        this.fileSize = PrPiMessageFile.getSizeOfFile(pathToFile, PrPiMessageFile.maxSizeFile); // Throws IOException, OutOfMemoryError
        this.message = getFileDataEncoded(pathToFile);  // Throws IOException, OutOfMemoryError
    }

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
    private PrPiMessageFile(@NotNull String transactionID, int nbMessage, int messageID, @NotNull String fileName, @NotNull String pathInProject, long fileSize, @NotNull String fileDataEncodedBase64) {
        super(transactionID, PrPiTransaction.FILE_TRANSFERT, nbMessage, messageID);
        this.fileName = fileName;
        this.pathInProject = pathInProject;
        this.fileSize = fileSize;
        this.message = fileDataEncodedBase64;
    }

    public boolean writeFile(Path projectBasePath) {
        Path path = Paths.get(projectBasePath.toString() + this.pathInProject);
        byte[] fileData = decodeFileData(this.message);
        try {
            Files.write(path, fileData);
        } catch (IOException e) {
            logger.error("Impossible to write the new file in this path " + path.toString(), e);
            return false;
        }
        return true;
    }

    /**
     * Create a set of all PrPiMessageFile to send the file, used when a single PrPiMessageFile is too small
     * @param pathToFile
     * @param projectBasePath
     * @return
     * @throws IOException
     */
    public static Map<Integer, PrPiMessageFile> createPrPiMessageFile(Path pathToFile, Path projectBasePath) throws IOException {

        PrPiMessageFile.isReadableAndIsNotDirectory(pathToFile); // Throws FileNotFoundException
        String fileName = pathToFile.getFileName().toString();
        String pathInProject = PrPiMessageFile.getPathToFileInProjectRoot(pathToFile, projectBasePath); // Throws FileNotFoundException
        long fileSize = PrPiMessageFile.getSizeOfFile(pathToFile);

        Map<Integer, PrPiMessageFile> allPrPiMessageFileForThisFile = new HashMap<>();

        FileInputStream fileInputStream = new FileInputStream(new File(pathToFile.toString()));


        byte[] fileData = new byte[PrPiMessageFile.maxSizeFile];

        int nbMessage = (int) (fileSize / PrPiMessageFile.maxSizeFile);
        if (fileSize % PrPiMessageFile.maxSizeFile != 0) {
            nbMessage++;
        }
        int messageNumber = 0;

        String transactionID = PrPiMessage.getNextID();

        while (fileSize > PrPiMessageFile.maxSizeFile) {

            if (fileInputStream.read(fileData) != PrPiMessageFile.maxSizeFile) {
                throw new IOException("File is currently changed !");
            }

            fileSize -= PrPiMessageFile.maxSizeFile;

            PrPiMessageFile segmentedFile = new PrPiMessageFile(transactionID, nbMessage, messageNumber, fileName, pathInProject, PrPiMessageFile.maxSizeFile, encodeFileData(fileData));
            logger.trace("Get byte from file (" + pathToFile + ") in the message ID : " + messageNumber + " / " + nbMessage);
            messageNumber++;

            allPrPiMessageFileForThisFile.put(segmentedFile.messageID, segmentedFile);

        }

        fileData = new byte[(int) fileSize];
        if (fileInputStream.read(fileData) != (int) fileSize) {
            throw new IOException("File is currently changed !");
        }

        PrPiMessageFile segmentedFile = new PrPiMessageFile(transactionID, nbMessage, messageNumber, fileName, pathInProject, PrPiMessageFile.maxSizeFile, encodeFileData(fileData));
        logger.trace("Get byte from file (" + pathToFile + ") in the message ID : " + messageNumber + " / " + nbMessage);
        allPrPiMessageFileForThisFile.put(segmentedFile.messageID, segmentedFile);

        return allPrPiMessageFileForThisFile;
    }

    public static boolean writeFileWithComposedMessages(Path projectBasePath, Map<Integer, PrPiMessageFile> messages) {
        PrPiMessageFile firstMessage = messages.get(0);
        Path path = Paths.get(projectBasePath.toString() + firstMessage.pathInProject);

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(new File(path.toString()));
            for (int i = 0; i < firstMessage.nbMessage; i++) {
                PrPiMessageFile message = messages.get(i);
                fileOutputStream.write(PrPiMessageFile.decodeFileData(message.message));
                logger.trace("Put byte in file (" + path + ") with the message ID : " + message.getMessageID() + " / " + (message.getNbMessage() - 1));
            }
        } catch (IOException e) {
            logger.error("Impossible to write the new file in this path " + path.toString(), e);
            return false;
        }
        return true;
    }

    private static void isReadableAndIsNotDirectory(Path pathToFile) throws FileNotFoundException {
        if (!Files.isReadable(pathToFile) || Files.isDirectory(pathToFile)) {
            throw new FileNotFoundException("The file isn't readable or is a directory ! File : " + pathToFile.toString());
        }
    }

    private static String getPathToFileInProjectRoot(Path pathToFile, Path projectBasePath) throws FileNotFoundException {
        if (projectBasePath != null && pathToFile.toString().startsWith(projectBasePath.toString())) {
            return pathToFile.toString().substring(projectBasePath.toString().length());
        } else {
            throw new FileNotFoundException("The project base path no corresponding with the path of the file. File path : " + pathToFile.toString());
        }
    }

    private static long getSizeOfFile(Path pathToFile) throws IOException {
        return Files.size(pathToFile);
    }

    private static long getSizeOfFile(Path pathToFile, int maxSize) throws IOException, OutOfMemoryError {
        long size = PrPiMessageFile.getSizeOfFile(pathToFile);
        if (size > maxSize) {
            throw new OutOfMemoryError("The file is too big (" + size + ", max is " + maxSize + ") ! File : " + pathToFile.toString());
        }
        return size;
    }

    private static String getFileDataEncoded(Path pathToFile) throws IOException, OutOfMemoryError {
        byte[] fileData = Files.readAllBytes(pathToFile);
        return PrPiMessageFile.encodeFileData(fileData);
    }

    public static String encodeFileData(byte[] fileData) {
        return Base64.getEncoder().encodeToString(fileData);
    }

    public static byte[] decodeFileData(String fileDataEncodedBase64) {
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
