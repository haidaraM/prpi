package com.prpi.network;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.apache.log4j.Logger;

import java.net.InetAddress;

public class PrPiServerHandler extends SimpleChannelInboundHandler<String> {

    static final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    static final Gson gson = new Gson();
    private static final Logger logger = Logger.getLogger(PrPiServerHandler.class);

    @Override
    public void channelActive(final ChannelHandlerContext ctx) {
        // Once session is secured, send a greeting and register the channel to the global channel
        // list so the channel received the messages from others.
        ctx.pipeline().get(SslHandler.class).handshakeFuture().addListener(
                future -> {
                    PrPiMessage response = new PrPiMessage(
                            "Welcome to " + InetAddress.getLocalHost().getHostName() + " secure remote project.\n" +
                                    "Your session is protected by " +
                                    ctx.pipeline().get(SslHandler.class).engine().getSession().getCipherSuite() +
                                    " cipher suite.\n"
                    );
                    String json = gson.toJson(response);
                    ctx.writeAndFlush(json);

                    channels.add(ctx.channel());
                });
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, String json) {

        logger.trace("Server reseive a new message");

        // Build result message
        try {
            PrPiMessage message = gson.fromJson(json, PrPiMessage.class);

            if (message.getVersion().equals(PrPiServer.PROTOCOL_PRPI_VERSION)) {
                if (message.isCloseConnection()) {
                    logger.debug("Client left the remote project.");
                    ctx.close();
                }

                logger.info("Message from a client : " + message.message.toString());
            } else {
                logger.error("Receive message from a client with a different version protocol (Server " + PrPiServer.PROTOCOL_PRPI_VERSION + " / Client " + message.getVersion() + ").");
            }

        } catch (JsonSyntaxException e) {
            logger.error("Error when parse the message to the json format.", e);
        }

        /*
        // Send the received message to all channels but the current one.
        for (Channel c: channels) {
            if (c != ctx.channel()) {
                c.writeAndFlush("[" + ctx.channel().remoteAddress() + "] " + msg + '\n');
            } else {
                c.writeAndFlush("[you] " + msg + '\n');
            }
        }

        // Close the connection if the client has sent 'bye'.
        if ("bye".equals(msg.toLowerCase())) {
            ctx.close();
        }*/
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("Error in server handler when reseive a new message", cause);
        ctx.close();
    }
}
