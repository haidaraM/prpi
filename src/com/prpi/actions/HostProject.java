package com.prpi.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.util.lang.UrlClassLoader;
import com.prpi.network.PrPiServerThread;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.helpers.Loader;

import java.io.IOException;
import java.net.URL;

public class HostProject extends AnAction {
    private static final Logger logger = LogManager.getLogger(HostProject.class);

    private Thread serverThread;
    private static final int DEFAULT_PORT = 4211;

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        URL s1 = UrlClassLoader.getSystemResource("config/log4j.xml");
        URL s2 = Loader.getResource("config/log4j.xml");
        URL s3 = ClassLoader.getSystemResource("config/log4j.xml");

        System.out.println("-------------");
        System.out.println(s1);
        System.out.println(s2);
        System.out.println(s3);

        PropertyConfigurator.configure("config/log4j.xml");

        logger.debug("Host projection action performed ended");
    }

    private void launchServerInThread(int port) throws IOException {
        serverThread = new PrPiServerThread(port);
        serverThread.start();
    }
}
