package com.prpi.network.client;

import com.intellij.openapi.project.Project;
import com.prpi.network.PrPiChannelInitializer;
import com.prpi.network.communication.PrPiMessage;
import com.prpi.network.server.PrPiServer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

@Deprecated
public class PrPiClient extends Thread {

    private String host;
    private int port;
    private Queue<String> messages = new LinkedList<>();
    private static final Logger logger = Logger.getLogger(PrPiClient.class);
    private Project currentProject = null;
    private EventLoopGroup group = null;
    private Channel channel = null;
    private PrPiClientHandler handler = null;


    public PrPiClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public PrPiClient(String host) {
        this(host, PrPiServer.DEFAULT_PORT);
    }

    public boolean initConnection() {

        if (this.currentProject == null) {
            logger.error("You need to setup the project of the client thread before run it !");
            return false;
        }

        logger.debug("Client begin his run ...");

        this.group = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap();
            b.group(group);
            b.channel(NioSocketChannel.class);

            handler = new PrPiClientHandler(currentProject);

            b.handler(new PrPiChannelInitializer(handler, host, port));

            // Start the connection attempt.
            this.channel = b.connect(host, port).sync().channel();

            // Add limit of 100000 in case of problem but this is an issue in big project (the copy process is long)
            for(int i = 0; !this.isProjectInitDone() && i < 1000000; i++)
                sleep(1000);

        } catch (InterruptedException | IOException e) {
            logger.error(e);
            return false;
        }
        return true;
    }

    public void closeConnection() {
        this.notify();
        try {
            this.wait();
        } catch (InterruptedException e) {
            logger.error(e);
        }
        logger.debug("Client connection closed");
    }

    @Override
    public void run() {

        if (this.group == null || this.channel == null) {
            logger.error("You need to init connection of the client thread before run it !");
            return;
        }

        logger.debug("Client begin his run ...");
        try {
            ChannelFuture lastWriteFuture = null;

            String message = "";
            while(message != null) {

                message = this.getMessageToSend();

                if (message != null) {
                    // Sends the received line to the server.
                    lastWriteFuture = this.channel.writeAndFlush(message);
                } else {
                    this.channel.closeFuture().sync();
                    break;
                }
            }

            // Wait until all messages are flushed before closing the channel.
            if (lastWriteFuture != null) {
                lastWriteFuture.sync();
            }

        } catch (InterruptedException e) {
            logger.error(e);
        } finally {
            // The connection is closed automatically on shutdown.
            this.group.shutdownGracefully();
            this.notify();
        }
        logger.debug("Client end his run.");
    }

    public synchronized void sendMessageToServer(PrPiMessage message) {
        logger.trace("Client add a new message in his pile");
        this.messages.add(message.toJson());
        this.notify();
    }

    private synchronized @Nullable String getMessageToSend() throws InterruptedException {
        while (this.messages.isEmpty()) {
            logger.trace("Client wait a new message to send");
            this.wait();
        }
        if (this.messages.isEmpty()) {
            return null;
        }
        return this.messages.remove();
    }

    public void setCurrentProject(Project currentProject) {
        this.currentProject = currentProject;
    }

    public boolean isProjectInitDone() {
        return this.handler.isProjectInitDone();
    }

    public String getProjectNameToSet() {
        if (this.isProjectInitDone()) {
            return this.handler.getProjectNameToSet();
        }
        return null;
    }
}
