package com.prpi.network.client;

import com.intellij.openapi.project.Project;
import com.prpi.network.communication.File;
import com.prpi.network.communication.NetworkTransaction;
import com.prpi.network.communication.NetworkTransactionRecomposer;
import com.prpi.network.communication.Transaction;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
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
    public void channelRead0(ChannelHandlerContext ctx, String json) throws Exception {

        logger.trace("Client receive a new NetworkTransaction.");

        Transaction transaction = this.recomposer.addPart(json);

        if (transaction != null) {

            logger.trace("The NetworkTransaciton complete a Transaction.");

            switch (transaction.getTransactionType()) {

                case FILE_TRANSFERT:
                    logger.trace("The transaction is a File");
                    String projectRootPath = project.getBasePath();
                    if (projectRootPath != null) {
                        File fileTransaction = (File) transaction;
                        if (fileTransaction.writeFile(Paths.get(projectRootPath))) {
                            logger.debug("A file was written in the project (" + projectRootPath + "/" + fileTransaction.getPathInProject() + "/" + fileTransaction.getFileName() + ")");
                        } else {
                            logger.error("Can't write this file : " + projectRootPath + "/" + fileTransaction.getPathInProject() + "/" + fileTransaction.getFileName());
                        }
                    } else {
                        logger.error("Can't get the project root path, the file can't be written !");
                    }
                    break;

                case SIMPLE_MESSAGE:
                    logger.trace("The transaction is a Message : " + transaction.toString());
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
