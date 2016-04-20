package com.prpi.network;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.json.JsonObjectDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.ssl.util.SelfSignedCertificate;

import javax.net.ssl.SSLException;
import java.security.cert.CertificateException;

public class ChannelInitializer extends io.netty.channel.ChannelInitializer<SocketChannel> {

    private final SslContext sslCtx;
    private String host;
    private int port;
    private SimpleChannelInboundHandler handler;

    public static final int MAX_FRAME_LENGTH = 1048576;

    /**
     * Constructor for Server side
     * @param handler
     * @throws SSLException
     * @throws CertificateException
     */
    public ChannelInitializer(SimpleChannelInboundHandler handler) throws SSLException, CertificateException {
        SelfSignedCertificate ssc = new SelfSignedCertificate();
        this.sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
        this.host = null;
        this.port = 0;
        this.handler = handler;
    }

    /**
     * Constructor for Client side
     * @param handler
     * @param host
     * @param port
     * @throws SSLException
     */
    public ChannelInitializer(SimpleChannelInboundHandler handler, String host, int port) throws SSLException {
        this.sslCtx = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
        this.host = host;
        this.port = port;
        this.handler = handler;
    }

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        // Add SSL handler first to encrypt and decrypt everything.
        // In this example, we use a bogus certificate in the server side
        // and accept any invalid certificates in the client side.
        // You will need something more complicated to identify both
        // and server in the real world.

        if (this.host != null && port != 0) { // Client side
            pipeline.addLast(sslCtx.newHandler(ch.alloc(), host, port));
        } else { // Server side
            pipeline.addLast(sslCtx.newHandler(ch.alloc()));
        }

        // On top of the SSL handler, add the text line codec.
        pipeline.addLast("framer", new JsonObjectDecoder(ChannelInitializer.MAX_FRAME_LENGTH));
        pipeline.addLast("decoder", new StringDecoder());
        pipeline.addLast("encoder", new StringEncoder());

        // and then business logic.
        pipeline.addLast("handler", this.handler);
    }
}
