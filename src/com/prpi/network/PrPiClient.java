package com.prpi.network;

import com.google.gson.Gson;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

public class PrPiClient extends Thread {

    private String host;
    private int port;
    private Queue<String> messages;
    private static final Logger logger = Logger.getLogger(PrPiClient.class);

    private static Gson gson = new Gson();

    public PrPiClient(String host, int port) {
        this.host = host;
        this.port = port;
        this.messages = new LinkedList<>();
    }

    public PrPiClient(String host) {
        this(host, PrPiServer.DEFAULT_PORT);
    }

    @Override
    public void run() {

        logger.debug("Client begin his run ...");

        EventLoopGroup group = new NioEventLoopGroup();

        try {
            // Configure SSL.
            final SslContext sslCtx = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();

            Bootstrap b = new Bootstrap();

            b.group(group);

            b.channel(NioSocketChannel.class);

            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ChannelPipeline pipeline = ch.pipeline();

                    // Add SSL handler first to encrypt and decrypt everything.
                    // In this example, we use a bogus certificate in the server side
                    // and accept any invalid certificates in the client side.
                    // You will need something more complicated to identify both
                    // and server in the real world.
                    pipeline.addLast(sslCtx.newHandler(ch.alloc(), host, port));

                    // On top of the SSL handler, add the text line codec.
                    pipeline.addLast("framer", new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
                    pipeline.addLast("decoder", new StringDecoder());
                    pipeline.addLast("encoder", new StringEncoder());

                    // and then business logic.
                    pipeline.addLast("handler", new PrPiClientHandler());
                }
            });

            // Start the connection attempt.
            Channel ch = b.connect(host, port).sync().channel();

            ChannelFuture lastWriteFuture;

            for (;;) {

                String message = this.getMessageToSend();
                logger.trace("Client get a new emssage to send");

                // Sends the received line to the server.
                lastWriteFuture = ch.writeAndFlush(message);

                // If user typed the 'bye' command, wait until the server closes
                // the connection.
                // TODO Implement a clean procedure to stop the client thread
                if (message.startsWith(PrPiServer.CLOSE_CONNECTION)) {
                    ch.closeFuture().sync();
                    break;
                }
            }

            // Wait until all messages are flushed before closing the channel.
            if (lastWriteFuture != null) {
                lastWriteFuture.sync();
            }

        } catch (InterruptedException | IOException e) {
            logger.error(e.getStackTrace());
        } finally {
            // The connection is closed automatically on shutdown.
            group.shutdownGracefully();
        }

        logger.debug("Client end his run.");
    }

    public synchronized void sendMessageToServer(PrPiMessage message) {
        logger.trace("Client add a new message in his pile");
        String json = gson.toJson(message);
        this.messages.add(json + "\n");
        this.notify();
    }

    private synchronized String getMessageToSend() throws InterruptedException {
        while (this.messages.isEmpty()) {
            logger.trace("Client wait a new message to send");
            this.wait();
        }
        return this.messages.remove();
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
