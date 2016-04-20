package com.prpi.network.client;

import com.intellij.openapi.project.Project;
import com.prpi.network.communication.NetworkTransactionRecomposer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.ssl.SslHandler;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Pierre on 19/04/2016.
 */
public class ClientTestHandler extends SimpleChannelInboundHandler<String> {

    private static Logger logger = Logger.getLogger(ClientTestHandler.class);



    public ClientTestHandler() {
        super();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String s) throws Exception {

    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.pipeline().get(SslHandler.class).handshakeFuture().addListener(
                future -> {
                    logger.debug("Client established connection to server: " + ctx);
                });
        ctx.close();
    }

}
