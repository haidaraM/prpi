package com.prpi.network.client;

import com.intellij.openapi.project.Project;
import com.prpi.network.communication.PrPiMessage;
import com.prpi.network.communication.PrPiMessageFile;
import com.prpi.network.communication.PrPiMessageRecomposer;
import com.prpi.network.server.PrPiServer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class PrPiClientHandler extends SimpleChannelInboundHandler<String> {
    private static Logger logger = Logger.getLogger(PrPiClientHandler.class);

    PrPiMessageRecomposer messageRecomposer = new PrPiMessageRecomposer();
    private Project currentProject;
    private boolean projectInitDone = false;
    private String projectNameToSet = null;

    public PrPiClientHandler(Project currentProject) {
        super();
        this.currentProject = currentProject;
    }

    /**
     * Called when client receives a message
     */
    @Override
    public void channelRead0(ChannelHandlerContext ctx, String json) throws Exception {
        logger.trace("Client - received a new message");

        PrPiMessage message = PrPiMessage.jsonToPrPiMessage(json);
        logger.debug("Received message : " + message.getTransactionID());

        if (!sameProtocolVersion(message)) {
            logger.error("Received message from the server has a different protocol version (Client : " + PrPiServer.PROTOCOL_PRPI_VERSION + " / Server : " + message.getVersion() + ").");
            return;
        }

        if (message.isComposedMessage()) {
            logger.debug("Processing a composed message...");
            processComposedMessage(ctx, message);
        } else {
            logger.debug("Processing a single message...");
            processMessage(ctx, message);
        }
    }

    private boolean sameProtocolVersion(PrPiMessage message) {
        return message.getVersion().equals(PrPiServer.PROTOCOL_PRPI_VERSION);
    }

    private void processComposedMessage(ChannelHandlerContext ctx, PrPiMessage message) {
        messageRecomposer.addPart(message);
        if (messageRecomposer.isFullyRecomposed()) {
            logger.debug("Processing a fully received composed message...");
            processMessages(ctx, messageRecomposer.getRecomposedMessages());
        } else {
            logger.debug("A part of a composed message received.");
        }
    }

    private void processMessages(ChannelHandlerContext ctx, List<PrPiMessage> messages) {
        PrPiMessage firstMessage = messages.get(0);
        switch (firstMessage.getTransaction()) {

            case FILE_TRANSFERT:
                Path projectBasePath = Paths.get(currentProject.getBasePath());
                List<PrPiMessageFile> messageFiles = ((List<PrPiMessageFile>) ((List<?>) messages));
                PrPiMessageFile.writeFiles(projectBasePath, messageFiles);
                break;

            default:
                logger.warn("Impossible to process this composed message, transaction unknown.");
                break;
        }
    }

    private void processMessage(ChannelHandlerContext ctx, PrPiMessage message) {
        switch (message.getTransaction()) {

            case CLOSE:
                // TODO Implement actions when server shutdown
                logger.debug("Server closes his connection.");
                ctx.close();
                break;

            case FILE_TRANSFERT:
                logger.debug("File transfert message from the server.");
                try {
                    PrPiMessageFile messageFile = (PrPiMessageFile) message;
                    Path projectBasePath = Paths.get(currentProject.getBasePath());

                    messageFile.writeFile(projectBasePath);

                    //logger.debug("Could write file : " + messageFile.getMessage());
                } catch (IOException e) {
                    logger.error("Couldn't write file", e);
                }
                break;

            case SIMPLE_MESSAGE:
                logger.trace("Simple message from the server : " + message.getMessage());
                break;

            case INIT_PROJECT:
                //logger.debug("Init message from the server : " + message);
                Map<String, Object> projectProperties = (Map<String, Object>) message.getMessage();
                this.projectNameToSet = (String) projectProperties.get("projectName");
                this.projectInitDone = true;
                break;

            default:
                logger.warn("Impossible to process this message, transaction unknown.");
                break;
        }
    }

    public boolean isProjectInitDone() {
        return this.projectInitDone;
    }

    public String getProjectNameToSet() {
        return projectNameToSet;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("Error in client handler", cause);
        super.exceptionCaught(ctx, cause);
    }
}