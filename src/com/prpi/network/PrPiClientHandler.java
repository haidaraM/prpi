package com.prpi.network;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PrPiClientHandler extends SimpleChannelInboundHandler<String> {
    private static Logger logger = Logger.getLogger(PrPiClientHandler.class);

    private Map<String, Set<PrPiMessage>> incompletePrPiMessage;

    public PrPiClientHandler() {
        super();
        incompletePrPiMessage = new HashMap<>();
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, String json) throws Exception {
        logger.trace("Client reseive a new message");

        PrPiMessage message = PrPiMessage.jsonToPrPiMessage(json);
        logger.debug("Message : " + message);
        if (message.getVersion().equals(PrPiServer.PROTOCOL_PRPI_VERSION)) {
            if (message.nbMessage > 1) {
                Set<PrPiMessage> composedPrPiMessage = this.addNewIncompletePrPiMessage(message);
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

    private @Nullable Set<PrPiMessage> addNewIncompletePrPiMessage(PrPiMessage message) {

        if (this.incompletePrPiMessage.containsKey(message.getTransactionID())) {
            Set<PrPiMessage> waitingPrPiMessage = this.incompletePrPiMessage.get(message.getTransactionID());
            waitingPrPiMessage.add(message);
            if (waitingPrPiMessage.size() == message.nbMessage) {
                this.incompletePrPiMessage.remove(message.getTransactionID());
                return waitingPrPiMessage;
            }
        } else {
            Set<PrPiMessage> waitingPrPiMessage = new HashSet<>();
            waitingPrPiMessage.add(message);
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
                // logger.debug("File write status : " + messageFile.writeFile(Paths.get("/tmp")));
                break;

            case SIMPLE_MESSAGE:
                logger.debug("Simple message from the server : " + message.message.toString());
                break;

            case INIT_PROJECT:
                // TODO
                logger.debug("Init message from the server.");
                break;
        }
    }

    private void processMessage(ChannelHandlerContext ctx, Set<PrPiMessage> messages) {
        // TODO
    }
}