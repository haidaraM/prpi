package com.prpi.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import com.prpi.PrPiApplicationComponent;
import com.prpi.PrPiProjectComponent;
import com.prpi.network.PrPiClient;
import com.prpi.network.PrPiMessage;
import com.prpi.network.PrPiServer;
import org.apache.log4j.Logger;

public class Test extends AnAction {

    private static final Logger logger = Logger.getLogger(Test.class);

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {

        // TODO Remove this class when the import project from wizard works

        // Permet de tester l'envoie de message depuis le client
        logger.trace("Test actionPerformed begin");

        PrPiProjectComponent projectApp = anActionEvent.getProject().getComponent(PrPiProjectComponent.class);

        if (!projectApp.isClient()) { // Create client before send a test message
            String ipAddress = Messages.showInputDialog("IP ?", "IP", Messages.getQuestionIcon(), "localhost", null);
            try {
                PrPiClient client = new PrPiClient(ipAddress, PrPiServer.DEFAULT_PORT);
                client.start();
                projectApp.setClientThread(client);
            } catch (Exception ex) {
                logger.error("Connection error", ex);
                Messages.showErrorDialog(anActionEvent.getProject(), ex.getMessage(), "Error Starting Server Thread");
            }
        }
        PrPiMessage<String> msg = new PrPiMessage<>("Test Bordel de merde !");
        projectApp.getClientThread().sendMessageToServer(msg);

        logger.trace("Test actionPerformed end");
    }

    @Override
    public void update(AnActionEvent anActionEvent) {
        super.update(anActionEvent);
    }
}
