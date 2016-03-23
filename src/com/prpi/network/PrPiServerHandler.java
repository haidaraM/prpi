package com.prpi.network;

import com.google.gson.JsonSyntaxException;
import com.intellij.openapi.project.Project;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.apache.log4j.Logger;

import java.nio.file.Paths;

public class PrPiServerHandler extends SimpleChannelInboundHandler<String> {

    static final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private static final Logger logger = Logger.getLogger(PrPiServerHandler.class);

    @Override
    public void channelActive(final ChannelHandlerContext ctx) {
        // Once session is secured, send a greeting and register the channel to the global channel
        // list so the channel received the messages from others.
        ctx.pipeline().get(SslHandler.class).handshakeFuture().addListener(
                future -> {

                    Project currentProject = PrPiServer.currentProject;
                    if (currentProject == null) {
                        logger.error("Problem in the initialization of the new client, the server hasn't a current project !");
                    } else {

                        // TODO Send the INIT_PROJECT file tree then all file to the client

                        PrPiMessageFile response = new PrPiMessageFile(Paths.get(currentProject.getBasePath() + "/HelloWorld.iml"), Paths.get(currentProject.getBasePath()));
                        String json = response.toJson();
                        logger.debug("Server send this file message to the client : " + json);
                        ctx.writeAndFlush(json);

//                        try {
//                            logger.debug("Base Path : " + PrPiServer.currentProject.getBasePath());
//                            long size = Files.walk(Paths.get(PrPiServer.currentProject.getBasePath())).mapToLong(p -> p.toFile().length() ).sum();
//                            logger.debug("Project size : " + size + " bytes");
//                        } catch (NullPointerException e) {
//                            logger.error(e);
//                        }

                    }


                    /* OLD :
                        PrPiMessage<String> response = new PrPiMessage<>(
                                "Welcome to " + InetAddress.getLocalHost().getHostName() + " secure remote project. " +
                                        "Your session is protected by " +
                                        ctx.pipeline().get(SslHandler.class).engine().getSession().getCipherSuite() +
                                        " cipher suite."
                        );
                        String json = response.toJson();
                        logger.debug("Server send this message to the client : " + json);
                        ctx.writeAndFlush(json);
                     */

                });
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, String json) {

        logger.trace("Server reseive a new message");

        // Build result message
        try {
            PrPiMessage message = PrPiMessage.jsonToPrPiMessage(json);

            if (message.getVersion().equals(PrPiServer.PROTOCOL_PRPI_VERSION)) {
                if (message.getTransaction().equals(PrPiTransaction.CLOSE)) {
                    logger.debug("Client left the remote project.");
                    channels.remove(ctx.channel());
                    ctx.close();
                }

                logger.debug("Message from a client : " + json);
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
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("Error in server handler", cause);
        super.exceptionCaught(ctx, cause);
    }
}
