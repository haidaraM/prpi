package com.prpi.network.client;

import com.intellij.openapi.project.Project;
import com.prpi.network.communication.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.ssl.SslHandler;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Paths;

public class ClientHandler extends SimpleChannelInboundHandler<String> {

    /**
     * The project that the client follow
     */
    private Project project;

    /**
     * The recomposer of Transaction
     */
    private NetworkTransactionRecomposer recomposer;

    private static Logger logger = Logger.getLogger(ClientHandler.class);

    public ClientHandler(@NotNull Project currentProject) {
        super();
        this.project = currentProject;
        this.recomposer = new NetworkTransactionRecomposer();
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
                    logger.debug("Client received a simple message: " + transaction.toString());
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
}
