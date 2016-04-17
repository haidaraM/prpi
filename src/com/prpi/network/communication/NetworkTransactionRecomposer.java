package com.prpi.network.communication;

import com.google.gson.JsonSyntaxException;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is able to recompose multiple incoming NetworkTransaction
 */
public class NetworkTransactionRecomposer {

    private static final Logger logger = Logger.getLogger(NetworkTransactionRecomposer.class);

    private Map<String, Map<Integer, NetworkTransaction>> incompleteMessages;

    public NetworkTransactionRecomposer() {
        this.incompleteMessages  = new HashMap<>();
    }

    /**
     * Add a part of receive message
     * @param json the part of message received
     * @return the Message if it complete, else return null
     */
    public Message addPart(String json) {
        Message result = null;
        try {
            // Get the NetworkTransaction
            NetworkTransaction networkTransaction = NetworkTransactionFactory.jsonToNetworkMessage(json);

            // Check if is a composed message
            if (networkTransaction.getNbMessage() > 1) {
                incompleteMessages.get(networkTransaction.getTransactionID()).put(networkTransaction.getMessageID(), networkTransaction);

                // Check if the composed message is completely here
                if (incompleteMessages.get(networkTransaction.getTransactionID()).size() == networkTransaction.getNbMessage()) {

                    // TODO How to choose between a file and a message ? Read PrPiMessageFileFactory

                    String content = "";
                    Map<Integer, NetworkTransaction> allComposedNetworkMessage = incompleteMessages.get(networkTransaction.getTransactionID());
                    for (int i = 0; i < networkTransaction.getNbMessage(); i++) {
                        content += allComposedNetworkMessage.get(i).getContent();
                    }
                    result = Message.jsonToMessage(content);
                }
            } else {
                result = Message.jsonToMessage(networkTransaction.getContent());
            }
        } catch (JsonSyntaxException e) {
            logger.error("The json is not correctly formed : " + json, e);
        }
        return result;
    }

}
