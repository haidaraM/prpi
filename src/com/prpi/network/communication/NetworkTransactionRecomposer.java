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
    private Map<String, Map<Integer, NetworkTransaction>> incompleteTransactions;

    /**
     * Contain all File not completly recomposed with his FileContent
     */
    private Map<String, File> incompleteFiles;

    /**
     * Contain all FileContent not attached to an File object
     */
    private Map<String, List<FileContent>> incompleteFileContents;

    public NetworkTransactionRecomposer() {
        this.incompleteTransactions = new HashMap<>();
        this.incompleteFiles = new HashMap<>();
        this.incompleteFileContents = new HashMap<>();
    }

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
            if (networkTransaction.isComposedMessage())
                resultTransaction = handleComposedMessage(networkTransaction);
            else // if simple message, simply parse message to the right object
                resultTransaction = handleSimpleMessage(networkTransaction);

            return resultTransaction;

        } catch (JsonSyntaxException e) {
            logger.error("The json is not correctly formed : " + json, e);
        } catch (ClassNotFoundException e) {
            logger.error("When converting the json to Transaction, the dynamic cast with his specified type can be done. The class given is not found (maybe you are not up to date with the last protocole version ?)");
        }
        return null;
    }

    private Transaction handleComposedMessage(NetworkTransaction networkTransaction) throws ClassNotFoundException {
        addTransaction(networkTransaction);

        Map<Integer, NetworkTransaction> composedNetworkMessages = incompleteTransactions.get(networkTransaction.getTransactionID());
        int expectedSize = networkTransaction.getNbMessage();
        if (composedNetworkMessages.size() != expectedSize) {
            logger.debug("NetworkTransaction is not complete. Need more parts");
            return null;
        }

        return recomposeTransaction(networkTransaction.getTransactionID());
    }

    private Transaction handleSimpleMessage(NetworkTransaction networkTransaction) {
        Transaction transaction = Transaction.jsonToTransaction(networkTransaction.getContent());
        return transaction;
    }

    private void addTransaction(NetworkTransaction transaction) {
        String transactionID = transaction.getTransactionID();
        Map<Integer, NetworkTransaction> currentTransactions = incompleteTransactions.get(transactionID);

        if (currentTransactions == null) {
            currentTransactions = new TreeMap<>();
            incompleteTransactions.put(transactionID, currentTransactions);
        }

        int messageID = transaction.getMessageID();
        currentTransactions.put(messageID, transaction);
    }

    private Transaction recomposeTransaction(String transactionId) throws ClassNotFoundException {

        Map<Integer, NetworkTransaction> composedNetworkMessages = incompleteTransactions.get(transactionId);

        // Recompose the transaction
        StringBuilder contentBuilder = new StringBuilder();
        for (NetworkTransaction transactionPart : composedNetworkMessages.values())
            contentBuilder.append(transactionPart.getContent());
        String content = contentBuilder.toString();

        // The recomposed transaction
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
        String fileId = file.getId();

        // If a file content is waiting for the file
        if (incompleteFileContents.containsKey(fileId)) {
            List<FileContent> fileContents = incompleteFileContents.get(file.getId());
            fileContents.forEach(file::addFileContent);

            incompleteFileContents.remove(fileId);
        }

        if (file.isComplete())
            return file;

        incompleteFiles.put(fileId, file);
        return null;
    }

    private File recomposeFileContent(FileContent fileContent) {
        String fileId = fileContent.getFileId();

        if (incompleteFiles.containsKey(fileId)) {
            File fileToComplete = incompleteFiles.get(fileId);
            fileToComplete.addFileContent(fileContent);

            if (fileToComplete.isComplete()) {
                incompleteFiles.remove(fileId);
                return fileToComplete;
            }
            else
                return null;

        } else {

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
