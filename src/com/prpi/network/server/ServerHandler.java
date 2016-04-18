package com.prpi.network.server;

import com.intellij.openapi.project.Project;
import com.prpi.network.communication.Message;
import com.prpi.network.communication.NetworkTransactionFactory;
import com.prpi.network.communication.NetworkTransactionRecomposer;
import com.prpi.network.communication.Transaction;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Paths;

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
                });
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, String json) {
        logger.debug("Server received a new message (NetworkTransaction).");

        Transaction transaction = recomposer.addPart(json);

        if (transaction != null) {

            logger.trace("The NetworkTransaciton complete a Transaction !");

            switch (transaction.getTransactionType()) {

                case INIT_PROJECT:
                    logger.debug("Received a request for project initialization");
                    /*sendProjectStructure();
                    sendProjectFiles();*/
                    break;

                case SIMPLE_MESSAGE:
                    logger.trace("The transaction is a Message : " + transaction.toString());
                    break;

                case PROJECT_FILES:
                    logger.trace("The transaction is a request to get all project files.");
                    NetworkTransactionFactory.buildAndSend(Paths.get(this.currentProject.getBasePath()), Paths.get(this.currentProject.getBasePath()), ctx.channel());
                    break;

                default:
                    logger.warn("Impossible to process this transaction, type is unsupported : " + transaction.toString());
                    break;
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("Error in server handler", cause);
        super.exceptionCaught(ctx, cause);
    }
}
