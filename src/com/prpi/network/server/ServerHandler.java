package com.prpi.network.server;

import com.intellij.openapi.project.Project;
import com.prpi.ProjectComponent;
import com.prpi.actions.DocumentActionsHelper;
import com.prpi.filesystem.HeartBeat;
import com.prpi.network.communication.*;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@ChannelHandler.Sharable
public class ServerHandler extends AbstractHandler {

    private static final ChannelGroup clientChannels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private static final Logger logger = Logger.getLogger(ServerHandler.class);

    public ServerHandler(@NotNull Project currentProject) {
        super(currentProject);
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) {
        // Once session is secured, send a greeting and register the channel to the global channel
        // list so the channel received the messages from others.
        ctx.pipeline().get(SslHandler.class).handshakeFuture().addListener(
                future -> {
                    logger.debug("Server received new connection: " + ctx);
                    clientChannels.add(ctx.channel());
                });
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, String json) {
        super.channelRead0(ctx, json);
        logger.debug("Server received a new message (NetworkTransaction).");

        if (!receivedTransactionIsComplete())
            return;

        Transaction transaction = getCompleteTransaction();

        if (responseIsAwaited(transaction.getTransactionID())) {
            addReceivedResponse(transaction);
            return;
        }

        // CAREFUL : if the received transaction needs a response, the response must have the same transactionID !!!
        switch (transaction.getTransactionType()) {

            case INIT_PROJECT:
                logger.debug("Received a request for project initialization");
                sendProjectFiles(ctx);
                break;

            case NUMBER_OF_PROJECT_FILES:
                sendNumberOfProjectFiles(transaction, ctx);
                break;

            case SIMPLE_MESSAGE:
                logger.info("Server received a simple message : " + transaction.toString());
                break;

            case PROJECT_NAME:
                logger.debug("Received request of project name");
                sendProjectName(transaction, ctx);
                break;

            case CLOSE:
                logger.debug("Server received a closing connection");
                ctx.close();
                clientChannels.remove(ctx.channel());
                break;
            case HEART_BEAT:
                logger.trace("New heart beat received : " + transaction.toString());
                HeartBeat heartBeat = ((Message<HeartBeat>) transaction).getContent();
                logger.debug("After cast, toString of the heartBeat : " + heartBeat.toString());

                ProjectComponent.getInstance().removeDocumentListenner();
                if (heartBeat.isInsertHeartBeat()) {
                    DocumentActionsHelper.insertStringInDocument(ProjectComponent.getInstance().getProject(),
                            heartBeat.getDocument(), heartBeat.getNewFragment(), heartBeat.getCaretOffset());
                } else {
                    DocumentActionsHelper.deleteStringIndocument(ProjectComponent.getInstance().getProject(),
                            heartBeat.getDocument(), heartBeat.getCaretOffset(), heartBeat.getCaretOffset() + 1);
                }

                ProjectComponent.getInstance().setupDocuementListener();

                break;

            default:
                logger.warn("Impossible to process this transaction, type is unsupported : " + transaction.toString());
                break;
        }
    }

    private void sendNumberOfProjectFiles(Transaction request, ChannelHandlerContext context) {
        // Make the response
        Message<Integer> response = new Message<>(
                NetworkTransactionFactory.getFilesCount(Paths.get(project.getBasePath())),
                Transaction.TransactionType.NUMBER_OF_PROJECT_FILES
        );

        // Set the transaction ID same as the request because its a response
        response.setTransactionID(request.getTransactionID());

        // Send
        NetworkTransactionFactory.buildAndSend(response, context.channel());
    }

    private void sendProjectFiles(ChannelHandlerContext context) {
        Path projectPath = Paths.get(project.getBasePath());
        NetworkTransactionFactory.buildAndSend(projectPath, projectPath, context.channel());
    }

    protected void sendTransactionToClients(Transaction msg) {
        for (Channel client : clientChannels) {
            NetworkTransactionFactory.buildAndSend(msg, client);
        }
    }

    private void sendProjectName(Transaction t, ChannelHandlerContext ctx) {
        Message<String> projectNameMessage = new Message<>(project.getName(), Transaction.TransactionType.PROJECT_NAME);
        projectNameMessage.setTransactionID(t.getTransactionID());
        logger.debug("Server sending project name: " + project.getName());
        NetworkTransactionFactory.buildAndSend(projectNameMessage, ctx.channel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("Error in server handler", cause);
        super.exceptionCaught(ctx, cause);
    }
}
