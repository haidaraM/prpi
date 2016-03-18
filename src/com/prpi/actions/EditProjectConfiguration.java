package com.prpi.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.prpi.PrPiApplicationComponent;
import com.prpi.network.PrPiServer;
import org.apache.log4j.Logger;
import com.prpi.network.PrPiClient;

public class EditProjectConfiguration extends AnAction {

    public static final String IP_CONF_NAME = "SHARED_MODULE_IP";
    public static final String PORT_CONF_NAME = "SHARED_MODULE_PORT";

    private static final Logger logger = Logger.getLogger(EditProjectConfiguration.class);

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        logger.debug("EditProjectConfiguration actionPerformed begin");
        Project project = anActionEvent.getProject();

        if (project == null) {
            logger.error("Error: no project found");
            return;
        }

        //NewProjectWizard wizard = new NewProjectWizard(null, ModulesProvider.EMPTY_MODULES_PROVIDER, null);
        //NewProjectUtil.createNewProject(getEventProject(anActionEvent), wizard);

        String ipAddress = Messages.showInputDialog(project, "IP ?", "IP", Messages.getQuestionIcon());
        try {
            PrPiApplicationComponent component = project.getComponent(PrPiApplicationComponent.class);
            if (component != null) {
                PrPiClient client = new PrPiClient(ipAddress, PrPiServer.DEFAULT_PORT);
                client.start();
                component.setClientThread(client);
            } else {
                logger.error("Error: no component found");
                return;
            }
        } catch (Exception ex) {
            logger.error("Connection error", ex);
            Messages.showErrorDialog(project, ex.getMessage(), "Error Starting Server Thread");
        }
        logger.debug("EditProjectConfiguration actionPerformed end");
    }
}
