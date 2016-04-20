package com.prpi.network.server;

import com.intellij.openapi.project.Project;
import com.prpi.network.communication.Message;
import com.prpi.network.communication.NetworkTransactionFactory;
import com.prpi.network.communication.NetworkTransactionRecomposer;
import com.prpi.network.communication.Transaction;
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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@ChannelHandler.Sharable
public class ServerHandler extends SimpleChannelInboundHandler<String> {

    private static final ChannelGroup clientChannels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private static final Logger logger = Logger.getLogger(ServerHandler.class);

    /**
     * The project that the client follow
     */
    private Project currentProject;

    private NetworkTransactionRecomposer recomposer;

    public ServerHandler(@NotNull Project currentProject) {
        super();
        this.currentProject = currentProject;
        this.recomposer = new NetworkTransactionRecomposer();
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) {
        // Once session is secured, send a greeting and register the channel to the global channel
        // list so the channel received the messages from others.
        ctx.pipeline().get(SslHandler.class).handshakeFuture().addListener(
                future -> {
                    logger.debug("Server received new connection: " + ctx);
                    /*Message welcomeMessage = new Message<>("Hello world new client !", Transaction.TransactionType.SIMPLE_MESSAGE);
                    NetworkTransactionFactory.buildAndSend(welcomeMessage, ctx.channel());*/
                    clientChannels.add(ctx.channel());
                });
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, String json) {
        logger.debug("Server received a new message (NetworkTransaction).");

        Transaction transaction = recomposer.addPart(json);

        if (transaction != null) {

            switch (transaction.getTransactionType()) {

                case INIT_PROJECT:
                    logger.debug("Received a request for project initialization");
                    sendProjectInfos(transaction, ctx);
                    sendProjectFiles(ctx);
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

                default:
                    logger.warn("Impossible to process this transaction, type is unsupported : " + transaction.toString());
                    break;
            }
        }
    }

    private void sendProjectInfos(Transaction request, ChannelHandlerContext context) {
        Map<String, Object> projectInfos = new HashMap<>();
        // TODO Check if getBasePath is null
        projectInfos.put("projectSize", NetworkTransactionFactory.getFilesCount(Paths.get(this.currentProject.getBasePath())));

        // Make the response
        Message<Map<String, Object>> response = new Message<>(projectInfos, Transaction.TransactionType.INIT_PROJECT);

        // Set the transaction ID same as the request because its a response
        response.setTransactionID(request.getTransactionID());
        logger.debug("The Transaction ID of the response : " + response.getTransactionID());

        // Send
        NetworkTransactionFactory.buildAndSend(response, context.channel());
    }

    private void sendProjectFiles(ChannelHandlerContext context) {
        Path projectPath = Paths.get(currentProject.getBasePath());
        NetworkTransactionFactory.buildAndSend(projectPath, projectPath, context.channel());
    }

    protected void sendTransactionToClients(Transaction msg) {
        for(Channel client : clientChannels) {
            NetworkTransactionFactory.buildAndSend(msg, client);
        }
    }

    private void sendProjectName(Transaction t, ChannelHandlerContext ctx) {
        Message<String> projectNameMessage = new Message<>(currentProject.getName(), Transaction.TransactionType.PROJECT_NAME);
        projectNameMessage.setTransactionID(t.getTransactionID());
        logger.debug("Server sending project name: " + currentProject.getName());
        NetworkTransactionFactory.buildAndSend(projectNameMessage, ctx.channel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("Error in server handler", cause);
        super.exceptionCaught(ctx, cause);
    }
}
