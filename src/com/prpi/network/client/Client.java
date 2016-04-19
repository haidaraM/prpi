package com.prpi.network.client;

import com.intellij.openapi.project.Project;
import com.prpi.network.ChannelInitializer;
import com.prpi.network.communication.Message;
import com.prpi.network.communication.NetworkTransactionFactory;
import com.prpi.network.communication.Transaction;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

public class Client {

    /**
     * The group used to communicate with the server
     */
    private NioEventLoopGroup group = new NioEventLoopGroup();

    /**
     * The handler, receive all requests from the server
     */
    private ClientHandler handler;

    /**
     * The communication channel used between the client and the server
     */
    private Channel channel = null;

    private static final Logger logger = Logger.getLogger(Client.class);

    /**
     * Create client to follow a project
     * @param currentProject the project to follow
     */
    public Client(@NotNull Project currentProject) {
        handler = new ClientHandler(currentProject);
    }

    /**
     * Setup the client to communicate with a server reachable with an host and port destination
     * @param host the host of the server
     * @param port the port of the server
     * @return true if the initialization is done, else false
     */
    public boolean connect(String host, int port) {
        logger.trace("Init connection of the client ...");
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(this.group);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.handler(new ChannelInitializer(handler, host, port));

            // Start the connection attempt.
            channel = bootstrap.connect(host, port).sync().channel();

        } catch (InterruptedException | IOException e) {
            logger.error(e);
            return false;
        }
        logger.trace("Init connection of the client done !");
        return true;
    }

    /**
     * Send the message to the server imediatly if the initialization of the client connection is done.
     * (the client not need to be run to use this method)
     * @param msg the Message to send
     */
    public void sendMessageToServer(@NotNull Message msg) throws InterruptedException {

        if (this.channel == null || !this.channel.isWritable()) {
            logger.error("You need to init connection of the client before send a message !");
            return;
        }

        List<ChannelFuture> lastWriteFuture = NetworkTransactionFactory.buildAndSend(msg, this.channel);

        // Sync all message before proccess others
        for (ChannelFuture channelFuture : lastWriteFuture) {
            if (channelFuture != null) {
                channelFuture.sync();
            }
        }
    }

    public void downloadProjetFiles() throws InterruptedException {
        this.sendMessageToServer(new Message<>("Foo", Transaction.TransactionType.INIT_PROJECT));
    }
}
