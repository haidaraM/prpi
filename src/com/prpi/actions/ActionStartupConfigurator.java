package com.prpi.actions;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.components.ApplicationComponent;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class ActionStartupConfigurator implements ApplicationComponent {
    private static final Logger logger = Logger.getLogger(ActionStartupConfigurator.class);

    @Override
    public void initComponent() {
        logger.debug("Init component");

        String type = PropertiesComponent.getInstance().getValue("type");
        logger.debug("Type : " + type);
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
