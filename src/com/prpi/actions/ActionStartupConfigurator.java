package com.prpi.actions;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.components.ApplicationComponent;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

public class ActionStartupConfigurator implements ApplicationComponent {
    private static final Logger logger = Logger.getLogger(ActionStartupConfigurator.class);

    @Override
    public void initComponent() {
        // Set the log4j configuration file
        DOMConfigurator.configure(getClass().getClassLoader().getResource("/log4j.xml"));

        logger.debug("Init component");

        logger.debug(PropertiesComponent.getInstance().getValue("version"));
        ActionManager actionManager = ActionManager.getInstance();
        // Look in configuration if this module is a SHARED_MODULE

        // if no, disable Edit shared project configurator

        logger.debug("End of component init");
    }

    @Override
    public void disposeComponent() {
        logger.debug("Dispose component");
    }

    @NotNull
    @Override
    public String getComponentName() {
        return "ActionStartupConfigurator";
    }
}
