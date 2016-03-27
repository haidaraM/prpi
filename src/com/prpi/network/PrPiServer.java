package com.prpi.network;

import com.intellij.openapi.project.Project;
import io.netty.bootstrap.ServerBootstrap;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import org.apache.log4j.Logger;

import javax.net.ssl.SSLException;
import java.security.cert.CertificateException;

public class PrPiServer extends Thread {

    public static final String DEFAULT_HOST = System.getProperty("host", "127.0.0.1");
    public static final int DEFAULT_PORT = Integer.parseInt(System.getProperty("port", "4211"));
    public static final String CLOSE_CONNECTION = "CLOSE_CONNECTION";
    public static final String PROTOCOL_PRPI_VERSION = "0.1";  // TODO Get this value from META-INF/plugin.xml directly

    private static final Logger logger = Logger.getLogger(PrPiServerHandler.class);

    private int port;
    private Channel channel;

    protected Project currentProject;

    public PrPiServer(int port, Project project) {
        this.port = port;
        this.currentProject = project;
        this.channel = null;
    }

    public PrPiServer(Project project) {
        this(DEFAULT_PORT, project);
    }

    public void run() {

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap();

            b.group(bossGroup, workerGroup);

            b.channel(NioServerSocketChannel.class);

            b.handler(new LoggingHandler(LogLevel.INFO));

            b.childHandler(new PrPiChannelInitializer(new PrPiServerHandler(this.currentProject)));

            // Bind and start to accept incoming connections.
            ChannelFuture f = b.bind(this.port).sync();

            this.channel = f.channel();

            // Wait until the server socket is closed.
            this.channel.closeFuture().sync();
        } catch (CertificateException | InterruptedException | SSLException e) {
            logger.error(e.getStackTrace());
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
            this.notify();
        }
    }

    public void closeConnection() {
        this.channel.close();
        try {
            this.wait();
        } catch (InterruptedException e) {
            logger.error(e);
        }
        logger.debug("Server connections closed");
    }
}