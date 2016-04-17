package com.prpi.network.communication;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Deprecated
public class PrPiMessageFileFactory {

    private static final Logger logger = Logger.getLogger(PrPiMessageFileFactory.class);

    public static List<List<PrPiMessageFile>> createFromDirectory(Path pathToDirectory, Path projectBasePath) {

        if (!Files.isReadable(pathToDirectory))
            return null;
        if (!Files.isDirectory(pathToDirectory))
            return null;

        File directory = pathToDirectory.toFile();
        File[] subFiles = getFilesToSend(directory);

        List<List<PrPiMessageFile>> filesInCurrentDirectory = new ArrayList<>();
        for (File subFile : subFiles) {
            if (subFile.isDirectory()) {
                List<List<PrPiMessageFile>> containedFiles = createFromDirectory(subFile.toPath(), projectBasePath);
                filesInCurrentDirectory.addAll(containedFiles);
            } else
                filesInCurrentDirectory.add(create(subFile.toPath(), projectBasePath));
        }

        return filesInCurrentDirectory;
    }

    private static File[] getFilesToSend(File directory) {
        return directory.listFiles((dir, name) -> !name.equals(".idea"));
    }

    /**
     * Create a set of all PrPiMessageFile to send the file, used when a single PrPiMessageFile is too small
     * @param pathToFile
     * @param projectBasePath
     * @return
     */
    private static List<PrPiMessageFile> create(Path pathToFile, Path projectBasePath) {

        if (!Files.isReadable(pathToFile))
            return null;
        if (Files.isDirectory(pathToFile))
            return null;

        String pathInProject = getPathToFileInProjectRoot(pathToFile, projectBasePath);
        int fileSize = PrPiMessageFile.getFileSize(pathToFile);
        logger.debug("Size of file " + pathToFile + " is " + fileSize);

        try (FileInputStream fileInputStream = new FileInputStream(pathToFile.toFile()))
        {
            int nbMessage = fileSize / PrPiMessageFile.MAX_SIZE_PER_MESSAGE;
            if (fileSize % PrPiMessageFile.MAX_SIZE_PER_MESSAGE > 0)
                nbMessage++;
            logger.debug("Must be cutted into " + nbMessage + " parts");
            List<PrPiMessageFile> allPrPiMessageFileForThisFile = new ArrayList<>();
            int messageNumber = 0;
            byte[] fileData;
            String transactionID = PrPiMessage.getNextID();
            String fileName = pathToFile.getFileName().toString();
            while (fileSize > PrPiMessageFile.MAX_SIZE_PER_MESSAGE) {

                fileData = new byte[PrPiMessageFile.MAX_SIZE_PER_MESSAGE];

                try {
                    int readSize = fileInputStream.read(fileData);
                    if (readSize != PrPiMessageFile.MAX_SIZE_PER_MESSAGE) {
                        logger.error("Not enough has been read from file (must be in use)");
                        return null;
                    }
                    fileSize -= PrPiMessageFile.MAX_SIZE_PER_MESSAGE;

                    PrPiMessageFile segmentedFile = new PrPiMessageFile(transactionID, nbMessage, messageNumber, fileName, pathInProject, PrPiMessageFile.MAX_SIZE_PER_MESSAGE, PrPiMessageFile.encodeFileData(fileData));
                    logger.trace("Get byte from file (" + pathToFile + ") in the message ID : " + messageNumber + " (" + (messageNumber+1) + " / " + nbMessage + ")");
                    logger.debug("Made part #" + messageNumber + " of message (size : " + readSize + ")");
                    messageNumber++;

                    allPrPiMessageFileForThisFile.add(segmentedFile);
                } catch (IOException e) {
                    logger.error("I/O problem when creating file message part", e);
                    return null;
                }
            }

            logger.debug("Making final part of message (" + fileSize + ")");
            fileData = new byte[fileSize];
            try {
                int readSize = fileInputStream.read(fileData);
                if (readSize != fileSize) {
                    logger.error("Not enough has been read from file");
                    return null;
                }
            } catch (IOException e) {
                logger.error("I/O problem when creating file message part", e);
                return null;
            }

            PrPiMessageFile segmentedFile = new PrPiMessageFile(transactionID, nbMessage, messageNumber, fileName, pathInProject, fileSize, PrPiMessageFile.encodeFileData(fileData));
            logger.trace("Get byte from file (" + pathToFile + ") in the message ID : " + messageNumber + " (" + (messageNumber+1) + " / " + nbMessage + ")");

            allPrPiMessageFileForThisFile.add(segmentedFile);

            logger.debug("Made " + allPrPiMessageFileForThisFile.size() + " parts of this message");
            return allPrPiMessageFileForThisFile;
        } catch (FileNotFoundException e) {
            logger.error("File not found error", e);
        } catch (IOException e) {
            logger.error("IO exception when reading file", e);
        }
        return null;
    }

    private static String getPathToFileInProjectRoot(Path pathToFile, Path projectBasePath) {
        if (pathToFile.toString().startsWith(projectBasePath.toString()))
            return pathToFile.toString().substring(projectBasePath.toString().length());

        return pathToFile.toString();
    }

}
