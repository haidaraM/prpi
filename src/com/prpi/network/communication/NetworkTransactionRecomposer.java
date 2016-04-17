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

    private Map<String, Map<Integer, NetworkTransaction>> incompleteTransactions;

    private Map<String, File> incompleteFiles;
    private Map<String, List<FileContent>> incompleteFileContents;

    public NetworkTransactionRecomposer() {
        this.incompleteTransactions = new HashMap<>();
        this.incompleteFiles = new HashMap<>();
        this.incompleteFileContents = new HashMap<>();
    }

    /**
     * Add a part of receive message
     * @param json the part of message received
     * @return the Transaction if it complete, else return null
     */
    public Transaction addPart(String json) {
        Transaction result = null;
        try {
            // Get the NetworkTransaction
            NetworkTransaction networkTransaction = NetworkTransactionFactory.jsonToNetworkMessage(json);

            // Check if is a composed message
            if (networkTransaction.getNbMessage() > 1) {
                incompleteTransactions.get(networkTransaction.getTransactionID()).put(networkTransaction.getMessageID(), networkTransaction);

                // Check if the composed message is completely here
                if (incompleteTransactions.get(networkTransaction.getTransactionID()).size() == networkTransaction.getNbMessage()) {

                    String content = "";
                    Map<Integer, NetworkTransaction> allComposedNetworkMessage = incompleteTransactions.get(networkTransaction.getTransactionID());
                    for (int i = 0; i < networkTransaction.getNbMessage(); i++) {
                        content += allComposedNetworkMessage.get(i).getContent();
                    }

                    result = Transaction.jsonToTransaction(content);

                    if (result instanceof File) {
                        File file = (File) result;
                        if (incompleteFileContents.containsKey(file.getId())) {
                            incompleteFileContents.get(file.getId()).forEach(file::addFileContent);
                            incompleteFileContents.remove(file.getId());
                        }

                        if (file.isComplete()) {
                            return file;
                        } else {
                            incompleteFiles.put(file.getId(), file);
                        }
                    } else if (result instanceof FileContent) {
                        FileContent fileContent = (FileContent) result;
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
                    } else {
                        return result;
                    }
                }
            } else {
                return Transaction.jsonToTransaction(networkTransaction.getContent());
            }
        } catch (JsonSyntaxException e) {
            logger.error("The json is not correctly formed : " + json, e);
        } catch (ClassNotFoundException e) {
            // TODO : error
        }
        return null;
    }

}
