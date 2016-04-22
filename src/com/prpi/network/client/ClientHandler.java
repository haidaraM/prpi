package com.prpi.network.client;

import com.intellij.openapi.project.Project;
import com.prpi.ProjectComponent;
import com.prpi.actions.DocumentActionsHelper;
import com.prpi.filesystem.HeartBeat;
import com.prpi.network.communication.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.ssl.SslHandler;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Paths;

class ClientHandler extends AbstractHandler {

    private static Logger logger = Logger.getLogger(ClientHandler.class);

    ClientHandler(@NotNull Project currentProject) {
        super(currentProject);
    }

    ClientHandler() {
        super();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.pipeline().get(SslHandler.class).handshakeFuture().addListener(
                future -> {
                    logger.debug("Client established connection to server: " + ctx);
                });

    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, String json) {
        super.channelRead0(ctx, json);
        logger.debug("Client received a new message");

        if (!receivedTransactionIsComplete())
            return;

        Transaction transaction = getCompleteTransaction();

        if (responseIsAwaited(transaction.getTransactionID())) {
            addReceivedResponse(transaction);
            return;
        }

        // CAREFUL : if the received transaction needs a response, the response must have the same transactionID !!!
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
                logger.trace("New heart beat received : " + transaction.toString());
                HeartBeat heartBeat = ((Message<HeartBeat>) transaction).getContent();
                logger.debug("After cast, toString of the heartBeat : " + heartBeat.toString());

                ProjectComponent realProjectComponent = (ProjectComponent) project.getComponent(ProjectComponent.getInstance().getComponentName());
                realProjectComponent.removeDocumentListener();
                if (heartBeat.isInsertHeartBeat()) {
                    DocumentActionsHelper.insertStringInDocument(project,
                            heartBeat.getDocument(project), heartBeat.getNewFragment(), heartBeat.getCaretOffset());
                } else {
                    DocumentActionsHelper.deleteStringInDocument(project,
                            heartBeat.getDocument(project), heartBeat.getCaretOffset(), heartBeat.getOldFragment().length());
                }

                realProjectComponent.setupDocumentListener();
                break;

            case CLOSE:
                ctx.close();
                break;

            default:
                logger.warn("Impossible to process this transaction, type is unsupported : " + transaction.toString());
                break;
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("Error in client handler", cause);
        super.exceptionCaught(ctx, cause);
    }
}
