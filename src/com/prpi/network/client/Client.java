package com.prpi.network.client;

import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.prpi.network.ChannelInitializer;
import com.prpi.network.communication.Message;
import com.prpi.network.communication.NetworkTransaction;
import com.prpi.network.communication.NetworkTransactionFactory;
import com.prpi.network.communication.Transaction;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

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

        if (msg.isWaitingResponse()) {
            // Set the transaction ID in the list of waiting response transaction
            this.handler.getTransactionResponse(msg.getTransactionID());
        }

        List<ChannelFuture> lastWriteFuture = NetworkTransactionFactory.buildAndSend(msg, this.channel);

        // Sync all message before proccess others
        for (ChannelFuture channelFuture : lastWriteFuture) {
            if (channelFuture != null) {
                channelFuture.sync();
            }
        }
    }

    /**
     * Initialize the download process of all project files from the remote project
     * @return the number of files to download
     * @throws InterruptedException
     */
    public int downloadProjetFiles() throws InterruptedException {
        Message<String> msg = new Message<>("Foo", Transaction.TransactionType.INIT_PROJECT);
        msg.setWaitingResponse(true);
        this.sendMessageToServer(msg);

        Transaction response = null;
        int timeout = 60;
        while(timeout > 0 && (response = this.handler.getTransactionResponse(msg.getTransactionID())) == null) {
            Thread.sleep(1000);
            timeout--;
        }
        if (response != null && response.getTransactionType() == Transaction.TransactionType.INIT_PROJECT) {
            Message<Map<String, Object>> responseMessage = (Message<Map<String, Object>>) response;
            Map<String, Object> projectInfos = responseMessage.getContent();
            if (projectInfos.containsKey("projectSize")) {
                // TODO Why deserialized give a Double insted of an int ??
                return ((Double)projectInfos.get("projectSize")).intValue();
            }
        }
        return -1;
    }

    public int getCurrentProjectSize() throws IOException {
        return NetworkTransactionFactory.getProjectSize(Paths.get(this.handler.getProject().getBasePath()));
    }



    private Client() {
        handler = new ClientHandler();
    }

    public static String sendProjectNameRequest(String ipAddress, int port) {
        try {
            Client c = new Client();
            c.connect(ipAddress, port);

            Message<String> msg = new Message<>("Bar", Transaction.TransactionType.PROJECT_NAME);
            msg.setWaitingResponse(true);
            logger.debug("Sending project name request to the server");
            c.sendMessageToServer(msg);

            Transaction response = null;
            int timeout = 20;
            while(timeout > 0 && (response = c.handler.getTransactionResponse(msg.getTransactionID())) == null) {
                Thread.sleep(300);
                timeout--;
            }

            msg = new Message<>("Foo bar", Transaction.TransactionType.CLOSE);
            c.sendMessageToServer(msg);
            //c.close();

            if (response != null && response.getTransactionType() == Transaction.TransactionType.PROJECT_NAME) {
                Message<String> responseMessage = (Message<String>) response;
                logger.debug("Client received project name from server: " + responseMessage.getContent());
                return responseMessage.getContent();
            }
            logger.warn("Could not get project name");
            return null;

        } catch (Exception e) {
            logger.error("Could not get project name from server", e);
            return null;
        }
    }
}
