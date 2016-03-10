package com.prpi.network;


import io.netty.bootstrap.ServerBootstrap;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * Discards any incoming data.
 */
public class PrpiServer {

    private int port;

    public PrpiServer(int port) {
        this.port = port;
    }

    public void run() throws Exception {
        /*
         NioEventLoopGroup is a multithreaded event loop that handles I/O operation.
         Netty provides various EventLoopGroup implementations for different kind of transports.
         We are implementing a server-side application in this example, and therefore two NioEventLoopGroup will be used.
         The first one, often called 'boss', accepts an incoming connection. The second one, often called 'worker',
         handles the traffic of the accepted connection once the boss accepts the connection and registers the accepted
         connection to the worker. How many Threads are used and how they are mapped to the created Channels depends on
         the EventLoopGroup implementation and may be even configurable via a constructor.
        */
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            /*
             ServerBootstrap is a helper class that sets up a server. You can set up the server using a Channel directly.
             However, please note that this is a tedious process, and you do not need to do that in most cases.
            */
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    /*
                     Here, we specify to use the NioServerSocketChannel class which is used to instantiate a new Channel
                     to accept incoming connections.
                    */
                    .channel(NioServerSocketChannel.class)
                    /*
                      The handler specified here will always be evaluated by a newly accepted Channel.
                      The ChannelInitializer is a special handler that is purposed to help a user configure a new Channel.
                      It is most likely that you want to configure the ChannelPipeline of the new Channel by adding some
                      handlers such as DiscardServerHandler to implement your network application. As the application
                      gets complicated, it is likely that you will add more handlers to the pipeline and extract this
                      anonymous class into a top level class eventually.
                    */
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new PrPiServerHandler());
                        }
                    })
                    /*
                     You can also set the parameters which are specific to the Channel implementation.
                     We are writing a TCP/IP server, so we are allowed to set the socket options such as tcpNoDelay
                     and keepAlive. Please refer to the apidocs of ChannelOption and the specific ChannelConfig
                     implementations to get an overview about the supported ChannelOptions.
                    */
                    .option(ChannelOption.SO_BACKLOG, 128)
                    /*
                     Did you notice option() and childOption()? option() is for the NioServerSocketChannel that accepts
                     incoming connections. childOption() is for the Channels accepted by the parent ServerChannel, which
                     is NioServerSocketChannel in this case.
                    */
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            // Bind and start to accept incoming connections.
            /*
              We are ready to go now. What's left is to bind to the port and to start the server. Here, we bind to the port
              8080 of all NICs (network interface cards) in the machine. You can now call the bind() method as many times
              as you want (with different bind addresses.)
            */
            ChannelFuture f = b.bind(port).sync(); // (7)

            // Wait until the server socket is closed.
            // In this example, this does not happen, but you can do that to gracefully
            // shut down your server.
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}