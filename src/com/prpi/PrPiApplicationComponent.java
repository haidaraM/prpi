package com.prpi;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.prpi.filesystem.PrPiVirtualFileListener;
import com.prpi.network.PrPiClient;
import com.prpi.network.PrPiServer;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.jetbrains.annotations.NotNull;


/**
 * For more documentation, see the followings links :
 *  - http://www.jetbrains.org/intellij/sdk/docs/basics/plugin_structure/plugin_components.html?search=comp
 *  - https://upsource.jetbrains.com/idea-ce/file/idea-ce-1731d054af4ca27aa827c03929e27eeb0e6a8366/platform/core-api/src/com/intellij/openapi/components/ApplicationComponent.java
 */
public class PrPiApplicationComponent implements ApplicationComponent {

    private static final Logger logger = Logger.getLogger(PrPiApplicationComponent.class);

    private PrPiServer serverThread;
    private PrPiClient clientThread;

    public PrPiApplicationComponent() {
        this.serverThread = null;
        this.clientThread = null;
    }

    @Override
    public void initComponent() {
        // Set the log4j configuration file
        DOMConfigurator.configure(getClass().getClassLoader().getResource("log4j.xml"));

        logger.debug("Init component");

        logger.debug(PropertiesComponent.getInstance());
        ActionManager actionManager = ActionManager.getInstance();
        // Look in configuration if this module is a SHARED_MODULE

        // if no, disable Edit shared project configurator

        // TODO: insert component initialization logic here
        setupDocuementListener();

        logger.debug("End of component init");
    }

    @Override
    public void disposeComponent() {
        // TODO: insert component disposal logic here
    }

    @Override
    @NotNull
    public String getComponentName() {
        return "PrPiApplicationComponent";
    }


    private void setupDocuementListener(){


        // TODO : maybe setupListenner asynchronously to avoid delays during application launch
        VirtualFileManager.getInstance().addVirtualFileListener(new PrPiVirtualFileListener());
    }

    @Override
    public String toString() {
        return "PrPiApplicationComponent{" +
                "serverThread=" + serverThread +
                ", clientThread=" + clientThread +
                '}';
    }

    public PrPiClient getClientThread() {
        return clientThread;
    }

    public void setClientThread(PrPiClient clientThread) {
        this.clientThread = clientThread;
    }

    public PrPiServer getServerThread() {
        return serverThread;
    }

    public void setServerThread(PrPiServer serverThread) {
        this.serverThread = serverThread;
    }
}
