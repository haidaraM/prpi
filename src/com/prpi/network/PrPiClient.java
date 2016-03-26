package com.prpi.network;

import com.intellij.openapi.ui.Messages;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutException;
import org.apache.log4j.Logger;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

public class PrPiClient extends Thread {

    private String host;
    private int port;
    private Queue<String> messages;
    private static final Logger logger = Logger.getLogger(PrPiClient.class);

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

            Bootstrap b = new Bootstrap();
            b.group(group);
            b.channel(NioSocketChannel.class);
            b.handler(new PrPiChannelInitializer(new PrPiClientHandler(), this.host, this.port));

            // Start the connection attempt.
            Channel ch = b.connect(this.host, this.port).sync().channel();

            ChannelFuture lastWriteFuture;

            for (;;) {

                String message = this.getMessageToSend();
                logger.trace("Client get a new message to send");

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
            logger.error(e);
        } finally {
            // The connection is closed automatically on shutdown.
            group.shutdownGracefully();
        }

        logger.debug("Client end his run.");
    }

    public synchronized void sendMessageToServer(PrPiMessage message) {
        logger.trace("Client add a new message in his pile");
        this.messages.add(message.toJson());
        this.notify();
    }

    private synchronized String getMessageToSend() throws InterruptedException {
        while (this.messages.isEmpty()) {
            logger.trace("Client wait a new message to send");
            this.wait();
        }
        return this.messages.remove();
    }

    public static synchronized boolean testConnection(String host, int port) {

        final boolean[] result = {false};

        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup);
            b.channel(NioSocketChannel.class);

            PrPiClientHandler testHandler = new PrPiClientHandler() {
                @Override
                public void channelRead0(ChannelHandlerContext ctx, String json) throws Exception {
                    PrPiMessage message = PrPiMessage.jsonToPrPiMessage(json);
                    if (!message.getVersion().equals(PrPiServer.PROTOCOL_PRPI_VERSION)) {
                        String messageError = "The test failed, different version protocol (Client " + PrPiServer.PROTOCOL_PRPI_VERSION + " / Server " + message.getVersion() + ").";
                        Messages.showErrorDialog(messageError, "PrPi Error - Protocol Version");
                        logger.error(messageError);
                    } else {
                        result[0] = true;
                    }
                    ctx.writeAndFlush(new PrPiMessage<>(true).toJson()).addListener(ChannelFutureListener.CLOSE);
                }
            };

            b.handler(new PrPiChannelInitializer(testHandler, host, port));

            // Start the connection attempt.
            ChannelFuture f = b.connect(host, port).sync();

            // Wait until the connection is closed.
            f.channel().closeFuture().sync();
        } catch (SSLException | InterruptedException e) {
            logger.error(e);
        } finally {
            workerGroup.shutdownGracefully();
        }
        return result[0];
    }
}
