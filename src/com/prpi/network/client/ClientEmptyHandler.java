package com.prpi.network.client;

import com.prpi.network.communication.AbstractHandler;
import com.prpi.network.communication.Transaction;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.ssl.SslHandler;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class ClientEmptyHandler extends AbstractHandler {

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
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String s) {

    }
}
