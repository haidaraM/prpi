package com.prpi.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.prpi.PrPiApplicationComponent;
import com.prpi.network.PrPiClient;
import com.prpi.network.PrPiMessage;
import org.apache.log4j.Logger;

public class Test extends AnAction {

    private static final Logger logger = Logger.getLogger(Test.class);

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {

        // Permet de tester l'envoie de message depuis le client
        logger.debug("Test actionPerformed begin");
        PrPiClient client = anActionEvent.getProject().getComponent(PrPiApplicationComponent.class).getClientThread();
        if (client != null) {
            PrPiMessage msg = new PrPiMessage("Test Bordel de merde !");
            client.sendMessageToServer(msg);
        } else {
            logger.error("No client started");
        }
        logger.debug("Test actionPerformed end");
    }
}
