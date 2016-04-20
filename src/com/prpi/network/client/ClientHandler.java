package com.prpi.network.client;

import com.intellij.openapi.project.Project;
import com.prpi.filesystem.HeartBeat;
import com.prpi.network.communication.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.ssl.SslHandler;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ClientHandler extends AbstractClientHandler {

    /**
     * The recomposer of Transaction
     */
    private NetworkTransactionRecomposer recomposer;

    /**
     * All responses corresponding to the transaction ID in the waitting list
     */
    private Map<String, Transaction> transactionResponses = new HashMap<>();

    private static Logger logger = Logger.getLogger(ClientHandler.class);

    ClientHandler(@NotNull Project currentProject) {
        super(currentProject);
        this.recomposer = new NetworkTransactionRecomposer();
    }

    ClientHandler() {
        super();
        recomposer = new NetworkTransactionRecomposer();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.pipeline().get(SslHandler.class).handshakeFuture().addListener(
                future -> {
                    logger.debug("Client established connection to server: " + ctx);
                });

    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, String json) throws Exception {
        logger.debug("Client received a new message");

        Transaction transaction = recomposer.addPart(json);

        if (transaction != null) {

            logger.trace("New Transaction (ID: " + transaction.getTransactionID() + ") - Responses availables : " + Arrays.toString(transactionResponses.keySet().toArray()));

            // If this is a transaction that corresponding to a response, it's not treated here, just put in the response list
            if (transactionResponses.containsKey(transaction.getTransactionID())) {
                Transaction resut = transactionResponses.get(transaction.getTransactionID());
                if (resut == null) {
                    transactionResponses.put(transaction.getTransactionID(), transaction);
                } else {
                    logger.error("Multiple response for the same transaction ID : " + transaction.getTransactionID());
                }
                return;
            }

            // If is not a reponse, need to be treated directly
            switch (transaction.getTransactionType()) {

                case FILE_TRANSFERT:
                    logger.trace("The message is a file transfert");
                    String projectRootPath = project.getBasePath();
                    File fileTransaction = (File) transaction;
                    if (fileTransaction.writeFile(Paths.get(projectRootPath))) {
                        logger.debug("A file was written in the project (" + projectRootPath + fileTransaction.getPathInProject() + ")");
                    } else {
                        logger.error("Can't write this file : " + projectRootPath + fileTransaction.getPathInProject());
                    }
                    break;

                case SIMPLE_MESSAGE:
                    logger.info("Client received a simple message: " + transaction.toString());
                    break;

                case HEART_BEAT:
                    logger.trace("New heart beat received : "+transaction.toString());
                    //HeartBeat heartBeat = (( Message<HeartBeat> )transaction).getContent();
                    break;

                case CLOSE:
                    ctx.close();
                    break;

                default:
                    logger.warn("Impossible to process this transaction, type is unsupported : " + transaction.toString());
                    break;
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("Error in client handler", cause);
        super.exceptionCaught(ctx, cause);
    }

    /**
     * Ask to get a response of a transaction ID
     * If there is a response, the transaction is return, else null is returned
     * @param transactionID the transaction ID of the transaction response
     * @return Transaction if the response arrived else null
     */
    @Override
    public @Nullable Transaction getTransactionResponse(@NotNull String transactionID) {
        if (transactionResponses.containsKey(transactionID)) {
            Transaction resut = transactionResponses.get(transactionID);
            if (resut != null) {
                transactionResponses.remove(transactionID);
                return resut;
            }
        } else {
            logger.debug("Add the waiting transaction ID : " + transactionID);
            transactionResponses.put(transactionID, null);
        }
        return null;
    }

}
