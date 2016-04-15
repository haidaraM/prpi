package com.prpi.network.communication;

import java.util.*;

/**
 * This class is able to recompose multiple incoming PrPiMessage
 */
public class PrPiMessageRecomposer {

    private Map<String, Map<Integer, PrPiMessage>> incompleteMessages = new HashMap<>();

    public void addPart(PrPiMessage message) {

        if (isKnownTransaction(message)) {
            Map<Integer, PrPiMessage> receivedMessages = incompleteMessages.get(message.getTransactionID());
            receivedMessages.put(message.getMessageID(), message);
        } else {
            Map<Integer, PrPiMessage> newIncomingMessages = new HashMap<>();
            newIncomingMessages.put(message.getMessageID(), message);
            incompleteMessages.put(message.getTransactionID(), newIncomingMessages);
        }
    }

    private boolean isKnownTransaction(PrPiMessage message) {
        return incompleteMessages.containsKey(message.getTransactionID());
    }

    /**
     * @return True if at least one message is complete
     */
    public boolean isFullyRecomposed() {
        for (Map<Integer, PrPiMessage> receivedMessages : incompleteMessages.values()) {
            int expectedNumberOfMessages = receivedMessages.get(0).getNbMessage();
            if (expectedNumberOfMessages == receivedMessages.size()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Removes internally the message from the list
     * @return A complete message
     */
    public List<PrPiMessage> getRecomposedMessages() {
        for (Map<Integer, PrPiMessage> receivedMessages : incompleteMessages.values()) {
            int expectedNumberOfMessages = receivedMessages.get(0).getNbMessage();
            if (expectedNumberOfMessages == receivedMessages.size()) {
                List<PrPiMessage> messages = new ArrayList<>(receivedMessages.values());
                incompleteMessages.remove(messages.get(0).getTransactionID());
                return messages;
            }
        }
        return null;
    }
}
