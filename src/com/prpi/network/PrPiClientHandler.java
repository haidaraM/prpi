package com.prpi.network;

import com.google.gson.JsonSyntaxException;
import com.intellij.openapi.project.Project;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class PrPiClientHandler extends SimpleChannelInboundHandler<String> {
    private static Logger logger = Logger.getLogger(PrPiClientHandler.class);

    private Map<String, Map<Integer, PrPiMessage>> incompletePrPiMessage;
    private Project currentProject;
    private boolean projectInitDone;
    private String projectNameToSet;

    public PrPiClientHandler(Project currentProject) {
        super();
        incompletePrPiMessage = new HashMap<>();
        this.currentProject = currentProject;
        this.projectInitDone = false;
        this.projectNameToSet = null;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, String json) throws Exception {
        logger.trace("Client reseive a new message");

        PrPiMessage message;
        try {
            message = PrPiMessage.jsonToPrPiMessage(json);
        } catch (JsonSyntaxException e) {
            logger.error("Problem during convertion Json to Object, the json : " + json, e);
            throw e;
        }
        logger.debug("Message : " + message);
        if (message.getVersion().equals(PrPiServer.PROTOCOL_PRPI_VERSION)) {
            if (message.nbMessage > 1) {
                Map<Integer, PrPiMessage> composedPrPiMessage = this.addNewIncompletePrPiMessage(message);
                if (composedPrPiMessage != null) {
                    logger.debug("Processing a composed message ...");
                    this.processMessage(ctx, composedPrPiMessage);
                } else {
                    logger.debug("A part of a composed message received : " + json);
                }
            } else {
                logger.debug("Processing a single message ...");
                this.processMessage(ctx, message);
            }

        } else {
            logger.error("Receive message from the server with a different version protocol (Client " + PrPiServer.PROTOCOL_PRPI_VERSION + " / Server " + message.getVersion() + ").");
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("Error in client handler", cause);
        super.exceptionCaught(ctx, cause);
    }

    private @Nullable Map<Integer, PrPiMessage> addNewIncompletePrPiMessage(PrPiMessage message) {

        if (this.incompletePrPiMessage.containsKey(message.getTransactionID())) {
            Map<Integer, PrPiMessage> waitingPrPiMessage = this.incompletePrPiMessage.get(message.getTransactionID());
            waitingPrPiMessage.put(message.messageID, message);
            if (waitingPrPiMessage.size() == message.nbMessage) {
                this.incompletePrPiMessage.remove(message.getTransactionID());
                return waitingPrPiMessage;
            }
        } else {
            Map<Integer, PrPiMessage> waitingPrPiMessage = new HashMap<>();
            waitingPrPiMessage.put(message.messageID, message);
            this.incompletePrPiMessage.put(message.getTransactionID(), waitingPrPiMessage);
        }
        return null;
    }

    private void processMessage(ChannelHandlerContext ctx, PrPiMessage message) {
        switch (message.getTransaction()) {

            case CLOSE:
                // TODO Implement actions when server shutdown
                logger.debug("Server close his connection.");
                ctx.close();
                break;

            case FILE_TRANSFERT:
                logger.debug("File message from the server.");
                PrPiMessageFile messageFile = (PrPiMessageFile) message;
                logger.debug("File write status : " + messageFile.writeFile(Paths.get(this.currentProject.getBasePath())));
                break;

            case SIMPLE_MESSAGE:
                logger.debug("Simple message from the server : " + message.message.toString());
                break;

            case INIT_PROJECT:
                logger.debug("Init message from the server : " + message);
                Map<String, Object> projectProperties = (Map<String, Object>) message.getMessage();
                this.projectNameToSet = (String) projectProperties.get("projectName");
                this.projectInitDone = true;
                break;

            default:
                logger.debug("Impossible to process this message, transaction unknown.");
                break;
        }
    }

    private void processMessage(ChannelHandlerContext ctx, Map<Integer, PrPiMessage> messages) {
        PrPiMessage firstMessage = messages.get(0);
        switch (firstMessage.getTransaction()) {

            case FILE_TRANSFERT:
                logger.debug("Porcess a composed file message ...");

                // TODO Do this more properly !
                @SuppressWarnings("unchecked")
                Map<Integer, PrPiMessageFile> messagesFile = (Map) messages;
                logger.debug("File write status : " + PrPiMessageFile.writeFromMessages(Paths.get(this.currentProject.getBasePath()), messagesFile));
                break;

            default:
                logger.debug("Impossible to process this composed message, transaction unknown.");
                break;
        }
    }

    public boolean isProjectInitDone() {
        return this.projectInitDone;
    }

    public String getProjectNameToSet() {
        return projectNameToSet;
    }
}