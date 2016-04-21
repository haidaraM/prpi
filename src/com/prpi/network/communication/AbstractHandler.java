package com.prpi.network.communication;

import com.intellij.openapi.project.Project;
import com.prpi.network.timeout.Timeout;
import com.prpi.network.timeout.TimeoutEnum;
import com.prpi.network.timeout.TimeoutFactory;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.TimeoutException;

public abstract class AbstractHandler extends SimpleChannelInboundHandler<String> implements ResponseWaiting {

    private static final Logger logger = Logger.getLogger(AbstractHandler.class);

    protected Project project = null;
    protected NetworkTransactionRecomposer recomposer = new NetworkTransactionRecomposer();

    private Transaction completeTransaction = null;


    public AbstractHandler(@NotNull Project project) {
        this.project = project;
    }

    public AbstractHandler() {
        super();
    }

    public Project getProject() {
        return project;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String json) {
        completeTransaction = recomposer.addPart(json);
    }

    protected boolean receivedTransactionIsComplete() {
        return completeTransaction != null;
    }

    protected Transaction getCompleteTransaction() {
        return completeTransaction;
    }


    // ------ Implementation of interface ResponseWaiting
    private Map<String, Transaction> receivedResponses = new HashMap<>();
    private Set<String> awaitedResponses = new HashSet<>();

    public boolean responseIsAwaited(@NotNull String transactionID) {
        return awaitedResponses.contains(transactionID);
    }

    public void addReceivedResponse(@NotNull Transaction transaction) {
        String id = transaction.getTransactionID();

        logger.debug("Received a response to transaction #" + id);
        awaitedResponses.remove(id);
        receivedResponses.put(id, transaction);
    }

    @Override
    public void setWaitingForResponse(@NotNull String transactionID) {
        boolean hasBeenAdded = awaitedResponses.add(transactionID);
        if (!hasBeenAdded) {
            logger.info("Already waiting for the response");
        }
    }


    @Override
    public Transaction waitForTransactionResponse(@NotNull String transactionID, TimeoutEnum timeoutEnum) throws TimeoutException {
        Timeout timeout = TimeoutFactory.create(timeoutEnum);

        if (receivedResponses.containsKey(transactionID)) {
            return receivedResponses.get(transactionID);
        }

        while (!receivedResponses.containsKey(transactionID)) {
            timeout.step(); // throws the TimeoutException if
        }

        return receivedResponses.get(transactionID);
    }
}
