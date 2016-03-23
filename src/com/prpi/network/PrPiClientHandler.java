package com.prpi.network;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.log4j.Logger;

import java.nio.file.Paths;

import static com.prpi.network.PrPiTransaction.*;

public class PrPiClientHandler extends SimpleChannelInboundHandler<String> {
    private static Logger logger = Logger.getLogger(PrPiClientHandler.class);

    @Override
    public void channelRead0(ChannelHandlerContext ctx, String json) throws Exception {
        logger.trace("Client reseive a new message");

        PrPiMessage message = PrPiMessage.jsonToPrPiMessage(json);
        if (message.getVersion().equals(PrPiServer.PROTOCOL_PRPI_VERSION)) {
            switch (message.getTransaction()) {

                case CLOSE:
                    // TODO Implement actions when server shutdown
                    logger.debug("Server close his connection.");
                    ctx.close();
                    break;

                case FILE_TRANSFERT:
                    PrPiMessageFile messageFile = PrPiMessageFile.jsonToPrPiMessageFile(json);
                    logger.debug("File message from the server : " + json);
                    // TODO uncomment when we know the real path to right the file
                    //logger.debug("File write status : " + messageFile.writeFile(Paths.get("/tmp")));
                    break;

                case SIMPLE_MESSAGE:
                    logger.debug("Simple message from the server : " + message.message.toString());
                    break;

                case INIT_PROJECT:
                    // TODO
                    logger.debug("Init message from the server.");
                    break;
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
}