package com.prpi.network;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.ReadTimeoutException;
import org.apache.log4j.Logger;

public class PrPiClientHandler extends SimpleChannelInboundHandler<String> {
    private static Logger logger = Logger.getLogger(PrPiClientHandler.class);

    @Override
    public void channelRead0(ChannelHandlerContext ctx, String json) throws Exception {
        logger.trace("Client reseive a new message");

        PrPiMessage message = PrPiMessage.jsonToPrPiMessage(json);
        if (message.getVersion().equals(PrPiServer.PROTOCOL_PRPI_VERSION)) {
            if (message.isCloseConnection()) {
                // TODO Implement actions when server shutdown
                logger.debug("Server close his connection.");
                ctx.close();
            }

            logger.debug("Message from the server : " + message.message.toString());
        } else {
            logger.error("Receive message from the server with a different version protocol (Client " + PrPiServer.PROTOCOL_PRPI_VERSION + " / Server " + message.getVersion() + ").");
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("Error in client handler", cause);
        super.exceptionCaught(ctx, cause);
    }
}