package com.prpi.network.server;

import com.google.gson.JsonSyntaxException;
import com.intellij.openapi.project.Project;
import com.prpi.network.communication.PrPiMessage;
import com.prpi.network.communication.PrPiMessageFile;
import com.prpi.network.communication.PrPiMessageFileFactory;
import com.prpi.network.communication.PrPiTransaction;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.apache.log4j.Logger;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Deprecated
public class PrPiServerHandler extends SimpleChannelInboundHandler<String> {

    private static final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private static final Logger logger = Logger.getLogger(PrPiServerHandler.class);

    protected Project currentProject;

    public PrPiServerHandler(Project currentProject) {
        super();
        this.currentProject = currentProject;
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) {
        // Once session is secured, send a greeting and register the channel to the global channel
        // list so the channel received the messages from others.
        ctx.pipeline().get(SslHandler.class).handshakeFuture().addListener(
                future -> {

                    Project currentProject = this.currentProject;
                    if (currentProject == null) {
                        logger.error("Problem in the initialization of the new client, the server hasn't a current project !");
                    } else {

                        // TODO Send the INIT_PROJECT file tree then all file to the client

                        // Works for small file ! -->
                        // Send a single message file
//                        PrPiMessageFile response = new PrPiMessageFile(Paths.get(currentProject.getBasePath() + "/HelloWorld.iml"), Paths.get(currentProject.getBasePath()));
//                        String json = response.toJson();
//                        logger.debug("Server send this file message to the client : " + json);
//                        ctx.writeAndFlush(json);

                        // Works for all files ! -->
                        // Send a composed message file
                        // Too big : PrPiMessageFile response = new PrPiMessageFile(Paths.get(currentProject.getBasePath() + "/test.txt"), Paths.get(currentProject.getBasePath()));
                        // So :
//                        Map<Integer, PrPiMessageFile> prPiMessagesFiles = PrPiMessageFile.create(Paths.get(currentProject.getBasePath() + "/test.txt"), Paths.get(currentProject.getBasePath()));
//                        prPiMessagesFiles.forEach((k,v)->{
//                            String json = v.toJson();
//                            logger.debug("Server send this file message to the client : " + json);
//                            ctx.writeAndFlush(json);
//                        });

                        // Works in progress -> Make th project init
                        Path projectDirectory = Paths.get(currentProject.getBasePath());
                        List<List<PrPiMessageFile>> allFilesMessages = PrPiMessageFileFactory.createFromDirectory(projectDirectory, projectDirectory);

                        // To store all transaction IDs
                        Set<String> allTransactionId = new HashSet<>();

                        allFilesMessages.forEach(l -> l.forEach(msg -> {
                            allTransactionId.add(msg.getTransactionID());
                            String json = msg.toJson();
                            logger.debug("Server send this file message to the client : " + json.getBytes().length + "(transaction id : " + msg.getTransactionID() + ")");
                            ctx.writeAndFlush(json);
                        }));

                        PrPiMessage<Map<String, Object>> initProjectMessage = new PrPiMessage<>(PrPiMessage.getNextID(), PrPiTransaction.INIT_PROJECT, 1, 0);

                        Map<String, Object> initProjectProperties = new HashMap<>();
                        initProjectProperties.put("transactionIDs", allTransactionId); // Put transactions
                        initProjectProperties.put("projectName", this.currentProject.getName()); // Put project name
                        initProjectMessage.setMessage(initProjectProperties);
                        String initProjectJson = initProjectMessage.toJson();
                        //logger.debug("Server send this init message project to the client : " + initProjectJson);
                        ctx.writeAndFlush(initProjectJson);


//                        try {
//                            logger.debug("Base Path : " + PrPiServer.currentProject.getBasePath());
//                            long size = Files.walk(Paths.get(PrPiServer.currentProject.getBasePath())).mapToLong(p -> p.toFile().length() ).sum();
//                            logger.debug("Project size : " + size + " bytes");
//                        } catch (NullPointerException e) {
//                            logger.error(e);
//                        }

                    }


                    /* OLD :
                        PrPiMessage<String> response = new PrPiMessage<>(
                                "Welcome to " + InetAddress.getLocalHost().getHostName() + " secure remote project. " +
                                        "Your session is protected by " +
                                        ctx.pipeline().get(SslHandler.class).engine().getSession().getCipherSuite() +
                                        " cipher suite."
                        );
                        String json = response.toJson();
                        logger.debug("Server send this message to the client : " + json);
                        ctx.writeAndFlush(json);
                     */
                });
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, String json) {

        logger.trace("Server receive a new message");

        // Build result message
        try {
            PrPiMessage message = PrPiMessage.jsonToPrPiMessage(json);

            if (message.getVersion().equals(PrPiServer.PROTOCOL_PRPI_VERSION)) {
                if (message.getTransaction().equals(PrPiTransaction.CLOSE)) {
                    logger.debug("Client left the remote project.");
                    //channels.remove(ctx.channel());
                    ctx.close();
                }

                logger.debug("Message from a client : " + json);
            } else {
                logger.error("Receive message from a client with a different version protocol (Server " + PrPiServer.PROTOCOL_PRPI_VERSION + " / Client " + message.getVersion() + ").");
            }

        } catch (JsonSyntaxException | ClassNotFoundException e) {
            logger.error("Error when parse the message to the json format.", e);
        }

        /*
        // Send the received message to all channels but the current one.
        for (Channel c: channels) {
            if (c != ctx.channel()) {
                c.writeAndFlush("[" + ctx.channel().remoteAddress() + "] " + msg + '\n');
            } else {
                c.writeAndFlush("[you] " + msg + '\n');
            }
        }

        // Close the connection if the client has sent 'bye'.
        if ("bye".equals(msg.toLowerCase())) {
            ctx.close();
        }*/
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("Error in server handler", cause);
        super.exceptionCaught(ctx, cause);
    }
}
