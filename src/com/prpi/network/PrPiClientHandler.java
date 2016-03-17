package com.prpi.network;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.log4j.Logger;

public class PrPiClientHandler extends SimpleChannelInboundHandler<String> {
    private static Logger logger = Logger.getLogger(PrPiClientHandler.class);
    static final Gson gson = new Gson();

    @Override
    public void channelRead0(ChannelHandlerContext ctx, String json) throws Exception {
        logger.trace("Client reseive a new message");

        // Build result message
        try {
            PrPiMessage message = gson.fromJson(json, PrPiMessage.class);

            if (message.getVersion().equals(PrPiServer.PROTOCOL_PRPI_VERSION)) {
                if (message.isCloseConnection()) {
                    // TODO Implement actions when server shutdown
                    logger.debug("Server close his connection.");
                    ctx.close();
                }

                logger.info("Message from the server : " + message.message.toString());
            } else {
                logger.error("Receive message from the server with a different version protocol (Client " + PrPiServer.PROTOCOL_PRPI_VERSION + " / Server " + message.getVersion() + ").");
            }

        } catch (JsonSyntaxException e) {
            logger.error("Error when parse the message to the json format.", e);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("Error in client handler when reseive a new message", cause);
        ctx.close();
    }
}