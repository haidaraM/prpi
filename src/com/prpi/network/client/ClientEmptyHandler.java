package com.prpi.network.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.ssl.SslHandler;
import org.apache.log4j.Logger;

/**
 * Created by Pierre on 19/04/2016.
 */
public class ClientEmptyHandler extends SimpleChannelInboundHandler<String> {

    private static Logger logger = Logger.getLogger(ClientEmptyHandler.class);

    public ClientEmptyHandler() {
        super();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.pipeline().get(SslHandler.class).handshakeFuture().addListener(
                future -> {
                    logger.debug("Client established connection to server: " + ctx);
                });
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String s) throws Exception {

    }

}
