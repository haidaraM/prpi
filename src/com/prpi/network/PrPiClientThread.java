package com.prpi.network;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.util.concurrent.*;

public class PrPiClientThread extends Thread {
    private PrPiClient client;

    public PrPiClientThread(String ip, int port) throws java.io.IOException {
        super();
        client = new PrPiClient(ip, port);
    }

    @Override
    public void run() {
        client.startListening();
    }

    public static Future<Boolean> testConnection(String ipAddress, int port) {
        Future<Boolean> futureTask = new FutureTask<>(() -> {
            final boolean[] result = {false};
            ChannelFuture channelFuture = new Bootstrap()
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            pipeline.addLast(new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
                            pipeline.addLast(new StringDecoder());
                            pipeline.addLast(new StringEncoder());
                            pipeline.addLast(new ChannelInboundHandlerAdapter() {
                                @Override
                                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                    if (msg.equals("OKAY BITCH"))
                                        result[0] = true;
                                }
                            });
                        }
                    })
                    .connect(ipAddress, port);
            channelFuture.await(2, TimeUnit.SECONDS);

            if (!channelFuture.isSuccess())
                return false;

            Channel channel = channelFuture.channel();
            channel.writeAndFlush("TEST_CONNECTION");
            channel.read();

            result[0] = true;

            channel.close();

            return result[0];
        });

        return futureTask;
    }
}
