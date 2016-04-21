package com.prpi.network.communication;

import com.google.gson.JsonSyntaxException;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * This class is able to recompose multiple incoming NetworkTransaction
 */
public class NetworkTransactionRecomposer {

    private static final Logger logger = Logger.getLogger(NetworkTransactionRecomposer.class);

    /**
     * Contain all Transaction not completly recomposed
     */
    private Map<String, Map<Integer, NetworkTransaction>> incompleteTransactions = new ConcurrentHashMap<>();

    /**
     * Contain all File not completly recomposed with his FileContent
     */
    private Map<String, File> incompleteFiles = new ConcurrentHashMap<>();

    /**
     * Contain all FileContent not attached to an File object
     */
    private Map<String, Map<Integer, FileContent>> incompleteFileContents = new ConcurrentHashMap<>();

    /**
     * Add a part of receive message (NetworkTransaction)
     * @param json String representing the NetworkTransaction in json
     * @return the Transaction if all parts are received, else return null
     *      It can be a Message or a File (if a File is return, all parts (FileContent) are present and the File can be write)
     */
    public @Nullable Transaction addPart(String json) {
        try {
            // Get the NetworkTransaction
            NetworkTransaction networkTransaction = NetworkTransactionFactory.jsonToNetworkMessage(json);

            logger.trace("Add NetworkTransaction part : " + json);

            Transaction resultTransaction;
            if (networkTransaction.isComposedMessage()) {
                resultTransaction = handleComposedMessage(networkTransaction);
            } else {
                // if simple message, simply parse message to the right object
                resultTransaction = handleTransactionContent(networkTransaction.getContent());
            }

            return resultTransaction;

        } catch (JsonSyntaxException e) {
            logger.error("The json is not correctly formed : " + json, e);
        } catch (ClassNotFoundException e) {
            logger.error("When converting the json to Transaction, the dynamic cast with his specified type can be done. The class given is not found (maybe you are not up to date with the last protocole version ?)");
        }
        return null;
    }

    /**
     * Proccess a new NetworkTransaction when it's a composed Transaction
     * @param networkTransaction the new NetworkTransaction received
     * @return a Transaction if the new NetworkTransaction complete a composed Transaction
     * @throws ClassNotFoundException
     */
    private @Nullable Transaction handleComposedMessage(NetworkTransaction networkTransaction) throws ClassNotFoundException {

        String transactionID = networkTransaction.getNetworkTransactionID();
        Map<Integer, NetworkTransaction> composedNetworkMessages = incompleteTransactions.get(transactionID);

        if (composedNetworkMessages == null) {
            composedNetworkMessages = new ConcurrentSkipListMap<>(); // Used to keep order in NetworkTransactions
            incompleteTransactions.put(transactionID, composedNetworkMessages);
        }

        int messageID = networkTransaction.getMessageID();
        composedNetworkMessages.put(messageID, networkTransaction);

        // Check if transaction if fully restored (all parts received)
        int expectedSize = networkTransaction.getNbMessage();
        if (composedNetworkMessages.size() != expectedSize) {
            logger.trace("NetworkTransaction is not complete. Need more parts");
            return null;
        }

        // Recompose the transaction
        StringBuilder contentBuilder = new StringBuilder();
        for (NetworkTransaction transactionPart : composedNetworkMessages.values()) {
            // Order is garanted by the TreeMap used
            contentBuilder.append(transactionPart.getContent());
        }
        String content = contentBuilder.toString();

        // The recomposed transaction
        return handleTransactionContent(content);
    }

    /**
     * Proccess a content of an NetworkTransaction or multiple when content is recomposed
     * @param content the content to convert in Transaction
     * @return the Transaction result of the converting proccess
     * @throws ClassNotFoundException
     */
    private @Nullable Transaction handleTransactionContent(String content) throws ClassNotFoundException {
        Transaction transaction = Transaction.jsonToTransaction(content);

        // If its a File or a FileContent, need to check if its fully recomposed too
        if (transaction instanceof File) {
            return recomposeFile((File) transaction);
        } else if (transaction instanceof FileContent) {
            return recomposeFileContent((FileContent) transaction);
        } else {
            return transaction;
        }

    }

    /**
     * Processing a new File, if it's completly recomposed with all his FileContent, It will be returned
     * @param file the new File
     * @return if File fully recomposed with his content, it's returned, else null
     */
    private @Nullable File recomposeFile(File file) {
        logger.trace("Recomposing file");
        String fileId = file.getId();

        // If a file content is waiting for the file
        if (incompleteFileContents.containsKey(fileId)) {
            logger.trace("Adding content to the file");
            Map<Integer, FileContent> fileContents = incompleteFileContents.get(file.getId());

            // TODO add a method in file to add all FileContent in one time
            fileContents.forEach((order,fileContent)->file.addFileContent(fileContent));

            incompleteFileContents.remove(fileId);
        } else {
            logger.trace("No file content associated (yet)");
        }

        if (file.isComplete()) {
            logger.trace("The file is now complete!");
            return file;
        }

        logger.trace("The file is uncomplete");
        incompleteFiles.put(fileId, file);
        return null;
    }

    /**
     * Adding a new FileContent to the corresponding File are addding then in a waiting file queue if the File it's not arrived yet
     * @param fileContent the new FileContent
     * @return the File corresponding to this FileContent, if the content of the File is fully recomposed
     */
    private @Nullable File recomposeFileContent(FileContent fileContent) {
        logger.trace("Recomposing file content");
        String fileId = fileContent.getFileId();

        if (incompleteFiles.containsKey(fileId)) {
            File fileToComplete = incompleteFiles.get(fileId);
            logger.trace("Adding the content the file");
            fileToComplete.addFileContent(fileContent);

            if (fileToComplete.isComplete()) {
                logger.trace("File is now complete!");
                incompleteFiles.remove(fileId);
                return fileToComplete;
            } else {
                logger.trace("File exists, but still not complete");
                return null;
            }

        } else {
            logger.trace("No file is associated with this content");
            Map<Integer, FileContent> fileContents = incompleteFileContents.get(fileId);
            if (fileContents == null) {
                fileContents = new ConcurrentHashMap<>();
                incompleteFileContents.put(fileId, fileContents);
            }
            fileContents.put(fileContent.getOrder(), fileContent);
            return null;
        }
    }
}
