package com.prpi.network.client;

import com.intellij.openapi.project.Project;
import com.prpi.network.PrPiChannelInitializer;
import com.prpi.network.communication.Message;
import com.prpi.network.communication.NetworkTransactionFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Client extends Thread {

    /**
     * The group used to communicate with the server
     */
    private NioEventLoopGroup group;

    /**
     * The handler, receive all requests from the server
     */
    private ClientHandler handler;

    /**
     * The communication channel used between the client and the server
     */
    private Channel channel;

    private Queue<Message> unsentMessages;

    private static final Logger logger = Logger.getLogger(Client.class);

    /**
     * Create client to follow a project
     * @param currentProject the project to follow
     */
    public Client(@NotNull Project currentProject) {
        this.group = new NioEventLoopGroup();
        this.handler = new ClientHandler(currentProject);
        this.channel = null;
        this.unsentMessages = new LinkedList<>();
    }

    /**
     * Setup the client to communicate with a server reachable with an host and port destination
     * @param host the host of the server
     * @param port the port of the server
     * @return true if the initialization is done, else false
     */
    public boolean initConnection(String host, int port) {
        logger.trace("Init connection of the client ...");
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(this.group);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.handler(new PrPiChannelInitializer(this.handler, host, port));

            // Start the connection attempt.
            this.channel = bootstrap.connect(host, port).sync().channel();

        } catch (InterruptedException | IOException e) {
            logger.error(e);
            return false;
        }
        logger.trace("Init connection of the client done !");
        return true;
    }

    @Override
    public void run() {

        if (this.channel == null || this.channel.isWritable()) {
            logger.error("You need to init connection of the client before run it !");
            return;
        }

        logger.debug("Client begin his run ...");
        try {
            // TODO Stop and close client !
            for(;;) {
                while (this.unsentMessages.isEmpty()) {
                    wait(10000);
                }
                Message msgToSend = this.unsentMessages.poll();
                if (msgToSend != null) {
                    List<ChannelFuture> lastWriteFuture = NetworkTransactionFactory.buildAndSend(msgToSend, this.channel);

                    // Sync all message before proccess others
                    for (ChannelFuture channelFuture : lastWriteFuture) {
                        if (channelFuture != null) {
                            channelFuture.sync();
                        }
                    }
                }
            }

        } catch (InterruptedException e) {
            logger.error(e);
        } finally {
            // The connection is closed automatically on shutdown.
            this.group.shutdownGracefully();
        }
        logger.debug("Client end his run.");
    }

    /**
     * Add a Message in the queue of message to send to the server
     * @param msg the Message to send
     */
    public void sendMessage(@NotNull Message msg) {
        this.unsentMessages.add(msg);
        this.notify();
    }
}
