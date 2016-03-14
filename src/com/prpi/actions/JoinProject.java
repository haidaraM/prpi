package com.prpi.actions;

import com.intellij.ide.impl.NewProjectUtil;
import com.intellij.ide.projectWizard.NewProjectWizard;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.prpi.network.PrPiServer;
import org.apache.log4j.Logger;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import com.prpi.network.PrPiClient;

import java.io.IOException;

public class JoinProject extends AnAction {
    private static final Logger logger = Logger.getLogger(JoinProject.class);

    private Thread clientThread;

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        logger.debug("JoinProject actionPerformed begin");
        Project project = anActionEvent.getProject();

        NewProjectWizard wizard = new NewProjectWizard(null, ModulesProvider.EMPTY_MODULES_PROVIDER, null);
        NewProjectUtil.createNewProject(getEventProject(anActionEvent), wizard);

        String ipAddress = Messages.showInputDialog(project, "IP ?", "IP", Messages.getQuestionIcon());
        try {
            joinServerInThread(ipAddress, PrPiServer.DEFAULT_PORT);
        } catch (IOException ex) {
            logger.error("Connection error", ex);
            Messages.showErrorDialog(project, ex.getMessage(), "Error Starting Server Thread");
        }
        logger.debug("JoinProject actionPerformed end");
    }

    public void joinServerInThread(String ip, int port) throws IOException {
        clientThread = new PrPiClient(ip, port);
        clientThread.start();
    }
}
