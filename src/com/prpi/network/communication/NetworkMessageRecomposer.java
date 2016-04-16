package com.prpi.network.communication;

import com.google.gson.JsonSyntaxException;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is able to recompose multiple incoming NetworkMessage
 */
public class NetworkMessageRecomposer {

    private static final Logger logger = Logger.getLogger(NetworkMessageRecomposer.class);

    private Map<String, Map<Integer, NetworkMessage>> incompleteMessages;

    public NetworkMessageRecomposer() {
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
            // Get the NetworkMessage
            NetworkMessage networkMessage = NetworkMessageFactory.jsonToNetworkMessage(json);

            // Check if is a composed message
            if (networkMessage.getNbMessage() > 1) {
                incompleteMessages.get(networkMessage.getTransactionID()).put(networkMessage.getMessageID(), networkMessage);

                // Check if the composed message is completely here
                if (incompleteMessages.get(networkMessage.getTransactionID()).size() == networkMessage.getNbMessage()) {
                    String content = "";
                    Map<Integer, NetworkMessage> allComposedNetworkMessage = incompleteMessages.get(networkMessage.getTransactionID());
                    for (int i=0; i < networkMessage.getNbMessage(); i++) {
                        content += allComposedNetworkMessage.get(i).getContent();
                    }
                    result = Message.jsonToMessage(content);
                }
            } else {
                result = Message.jsonToMessage(networkMessage.getContent());
            }
        } catch (JsonSyntaxException e) {
            logger.error("The json is not correctly formed : " + json, e);
        }
        return result;
    }

}
