package com.prpi.network.communication;

import com.google.gson.JsonSyntaxException;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * This class is able to recompose multiple incoming NetworkTransaction
 */
public class NetworkTransactionRecomposer {

    private static final Logger logger = Logger.getLogger(NetworkTransactionRecomposer.class);

    /**
     * Contain all Transaction not completly recomposed
     */
    private Map<String, Map<Integer, NetworkTransaction>> incompleteTransactions = new HashMap<>();

    /**
     * Contain all File not completly recomposed with his FileContent
     */
    private Map<String, File> incompleteFiles = new HashMap<>();

    /**
     * Contain all FileContent not attached to an File object
     */
    private Map<String, List<FileContent>> incompleteFileContents = new HashMap<>();

    /**
     * Add a part of receive message (NetworkTransaction)
     * @param json String representing the NetworkTransaction in json
     * @return the Transaction if all parts are received, else return null
     *      It can be a Message or a File (if a File is return, all parts (FileContent) are present and the File can be write)
     */
    public Transaction addPart(String json) {
        try {
            // Get the NetworkTransaction
            NetworkTransaction networkTransaction = NetworkTransactionFactory.jsonToNetworkMessage(json);

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
    private Transaction handleComposedMessage(NetworkTransaction networkTransaction) throws ClassNotFoundException {

        // TODO Why use method here ? You need to research in the incompleteTransactions map again after :/
        addPartOfTransaction(networkTransaction);

        // Check if transaction if fully restored (all parts received)
        String transactionId = networkTransaction.getTransactionID();
        Map<Integer, NetworkTransaction> composedNetworkMessages = incompleteTransactions.get(transactionId);
        int expectedSize = networkTransaction.getNbMessage();
        if (composedNetworkMessages.size() != expectedSize) {
            logger.debug("NetworkTransaction is not complete. Need more parts");
            return null;
        }

        // Recompose the transaction
        StringBuilder contentBuilder = new StringBuilder();
        for (NetworkTransaction transactionPart : composedNetworkMessages.values()) {

            // TODO the order is respected in every cases ?
            contentBuilder.append(transactionPart.getContent());
        }
        String content = contentBuilder.toString();

        // The recomposed transaction
        return handleTransactionContent(content);
    }

    private void addPartOfTransaction(NetworkTransaction transaction) {
        String transactionID = transaction.getTransactionID();
        Map<Integer, NetworkTransaction> currentTransactions = incompleteTransactions.get(transactionID);

        if (currentTransactions == null) {
            currentTransactions = new HashMap<>();
            incompleteTransactions.put(transactionID, currentTransactions);
        }

        int messageID = transaction.getMessageID();
        currentTransactions.put(messageID, transaction);
    }

    /**
     * Proccess a content of an NetworkTransaction or multiple when content is recomposed
     * @param content the content to convert in Transaction
     * @return the Transaction result of the converting proccess
     * @throws ClassNotFoundException
     */
    private Transaction handleTransactionContent(String content) throws ClassNotFoundException {
        Transaction transaction = Transaction.jsonToTransaction(content);

        // If its a File or a FileContent, need to check if its fully recomposed too
        if (transaction instanceof File) // TODO use TransactionType instead ?? What is the best ?
            return recomposeFile((File) transaction);
        else if (transaction instanceof FileContent) // TODO use TransactionType instead ?? What is the best ?
            return recomposeFileContent((FileContent) transaction);
        else
            return transaction;
    }

    private File recomposeFile(File file) {
        logger.trace("Recomposing file");
        String fileId = file.getId();

        // If a file content is waiting for the file
        if (incompleteFileContents.containsKey(fileId)) {
            logger.trace("Adding content to the file");
            List<FileContent> fileContents = incompleteFileContents.get(file.getId());
            fileContents.forEach(file::addFileContent);

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

    private File recomposeFileContent(FileContent fileContent) {
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
            }
            else {
                logger.trace("File exists, but still not complete");
                return null;
            }

        } else {
            logger.trace("No file is associated with this content");
            List<FileContent> fileContents = incompleteFileContents.get(fileId);
            if (fileContents == null) {
                fileContents = new ArrayList<>();
                incompleteFileContents.put(fileId, fileContents);
            }
            fileContents.add(fileContent);
            return null;
        }
    }
}
