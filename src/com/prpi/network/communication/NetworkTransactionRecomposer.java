package com.prpi.network.communication;

import com.google.gson.JsonSyntaxException;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

            // Check if is a composed message
            if (networkTransaction.getNbMessage() > 1) {
                incompleteTransactions.get(networkTransaction.getTransactionID()).put(networkTransaction.getMessageID(), networkTransaction);

                // Check if the composed message is completely here
                if (incompleteTransactions.get(networkTransaction.getTransactionID()).size() == networkTransaction.getNbMessage()) {

                    // Recomposed the Transaction with all NetworkTransaction
                    String content = "";
                    Map<Integer, NetworkTransaction> allComposedNetworkMessage = incompleteTransactions.get(networkTransaction.getTransactionID());
                    for (int i = 0; i < networkTransaction.getNbMessage(); i++) {
                        content += allComposedNetworkMessage.get(i).getContent();
                    }

                    // The recomposed Transaction
                    Transaction transaction = Transaction.jsonToTransaction(content);

                    // If its a File or a FileContent, need to check if its fully recomposed too
                    if (transaction instanceof File) { // TODO use TransactionType instead ?? What is the best ?
                        File file = (File) transaction;
                        if (incompleteFileContents.containsKey(file.getId())) {
                            incompleteFileContents.get(file.getId()).forEach(file::addFileContent);
                            incompleteFileContents.remove(file.getId());
                        }

                        if (file.isComplete()) {
                            return file;
                        } else {
                            incompleteFiles.put(file.getId(), file);
                        }
                    } else if (transaction instanceof FileContent) { // TODO use TransactionType instead ?? What is the best ?
                        FileContent fileContent = (FileContent) transaction;
                        if (incompleteFiles.containsKey(fileContent.getFileId())) {
                            File fileToComplete = incompleteFiles.get(fileContent.getFileId());
                            fileToComplete.addFileContent(fileContent);
                            if (fileToComplete.isComplete()) {
                                return fileToComplete;
                            }
                        } else {
                            if (incompleteFileContents.containsKey(fileContent.getFileId())) {
                                incompleteFileContents.get(fileContent.getFileId()).add(fileContent);
                            } else {
                                List<FileContent> listFileContents = new ArrayList<>();
                                listFileContents.add(fileContent);
                                incompleteFileContents.put(fileContent.getFileId(), listFileContents);
                            }
                        }

                    // Else its a simple Message and can be given
                    } else {
                        return transaction;
                    }
                }
            } else {
                // Its a Transaction in a single NetworkTransaction, can be given directly
                return Transaction.jsonToTransaction(networkTransaction.getContent());
            }
        } catch (JsonSyntaxException e) {
            logger.error("The json is not correctly formed : " + json, e);
        } catch (ClassNotFoundException e) {
            logger.error("When converting the json to Transaction, the dynamic cast with his specified type can be done. The class given is not found (maybe you are not up to date with the last protocole version ?)");
        }
        return null;
    }

}
