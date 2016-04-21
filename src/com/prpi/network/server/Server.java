package com.prpi.network.server;

import com.intellij.openapi.project.Project;
import com.prpi.network.ChannelInitializer;
import com.prpi.network.communication.AbstractHandler;
import com.prpi.network.communication.Message;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import javax.net.ssl.SSLException;
import java.security.cert.CertificateException;

public class Server extends Thread {

    /**
     * The listen port of the server
     */
    public static final int DEFAULT_PORT = Integer.parseInt(System.getProperty("port", "4211"));

    private static final Logger logger = Logger.getLogger(Server.class);

    /**
     * The listen port of the server
     */
    private int listenPort;

    /**
     * The handler, receive all requests from clients
     */
    private ServerHandler handler;

    public Server(@NotNull Project project, int port) {
        this.handler = new ServerHandler(project);
        this.listenPort = port;
    }

    public Server(@NotNull Project project) {
        this(project, Server.DEFAULT_PORT);
    }

    @Override
    public void run() {

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();

            serverBootstrap.group(bossGroup, workerGroup);

            serverBootstrap.channel(NioServerSocketChannel.class);

            serverBootstrap.handler(new LoggingHandler(LogLevel.INFO));

            serverBootstrap.childHandler(new ChannelInitializer(this.handler));

            // Bind and start to accept incoming connections.
            ChannelFuture f = serverBootstrap.bind(this.listenPort).sync();

            // Wait until the server socket is closed.
            f.channel().closeFuture().sync();

        } catch (CertificateException | InterruptedException | SSLException e) {
            logger.error(e);
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
            this.notify();
        }
    }

    public void sendMessageToClients(Message msg) {
        handler.sendTransactionToClients(msg);
    }
}