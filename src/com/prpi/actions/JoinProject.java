package com.prpi.actions;

import com.intellij.ide.impl.NewProjectUtil;
import com.intellij.ide.projectWizard.NewProjectWizard;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import com.intellij.openapi.ui.Messages;
import com.prpi.network.PrPiClientThread;
import com.prpi.network.PrpiServer;

import java.io.IOException;

public class JoinProject extends AnAction {

    private Thread clientThread;

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {

        System.out.println("JoinProject ACTION PERFORMED");
        Project project = anActionEvent.getProject();
        NewProjectWizard wizard = new NewProjectWizard(null, ModulesProvider.EMPTY_MODULES_PROVIDER, null);

        NewProjectUtil.createNewProject(getEventProject(anActionEvent), wizard);

        String ipAddress = Messages.showInputDialog(project, "IP ?", "IP", Messages.getQuestionIcon());
        if(ipAddress != null){
            try {
                joinServerInThread(ipAddress, PrpiServer.DEFAULT_PORT);
            } catch (IOException ex) {
                Messages.showErrorDialog(project, ex.getMessage(), "Error Starting Server Thread");
            }
        }
    }

    public void joinServerInThread(String ip, int port) throws IOException {
        clientThread = new PrPiClientThread(ip, port);
        clientThread.start();
    }
}
