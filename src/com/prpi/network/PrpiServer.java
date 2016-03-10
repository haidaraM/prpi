package com.prpi.network;

import io.netty.bootstrap.ServerBootstrap;

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

import javax.net.ssl.SSLException;
import java.security.cert.CertificateException;

public class PrpiServer extends Thread {

    public static final String DEFAULT_HOST = System.getProperty("host", "127.0.0.1");
    public static final int DEFAULT_PORT = Integer.parseInt(System.getProperty("port", "4211"));

    private int port;

    public PrpiServer(int port) {
        this.port = port;
    }

    public PrpiServer() {
        this.port = DEFAULT_PORT;
    }

    public void run() {

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            SelfSignedCertificate ssc = new SelfSignedCertificate();

            SslContext sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();

            ServerBootstrap b = new ServerBootstrap();

            b.group(bossGroup, workerGroup);

            b.channel(NioServerSocketChannel.class);

            b.handler(new LoggingHandler(LogLevel.INFO));

            b.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ChannelPipeline pipeline = ch.pipeline();

                    // Add SSL handler first to encrypt and decrypt everything.
                    // In this example, we use a bogus certificate in the server side
                    // and accept any invalid certificates in the client side.
                    // You will need something more complicated to identify both
                    // and server in the real world.
                    pipeline.addLast(sslCtx.newHandler(ch.alloc()));

                    // On top of the SSL handler, add the text line codec.
                    pipeline.addLast(new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
                    pipeline.addLast(new StringDecoder());
                    pipeline.addLast(new StringEncoder());

                    // and then business logic.
                    pipeline.addLast(new PrPiServerHandler());
                }
            });

            // Bind and start to accept incoming connections.
            ChannelFuture f = b.bind(port).sync();

            // Wait until the server socket is closed.
            f.channel().closeFuture().sync();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (SSLException e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}