package com.prpi;

import com.intellij.openapi.project.Project;
import com.prpi.network.client.Client;
import com.prpi.network.communication.Message;
import com.prpi.network.server.Server;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class ProjectComponent implements com.intellij.openapi.components.ProjectComponent {

    private Server server;
    private Client client;
    private Project project;

    private static ProjectComponent instance;

    private static Logger logger = Logger.getLogger(ProjectComponent.class);

    public ProjectComponent(Project project) {
        this.project = project;
        instance = this;
    }

    public static ProjectComponent getInstance() {
        return instance;
    }

    @Override
    public void initComponent() {
        // TODO: insert component initialization logic here
    }

    @Override
    public void disposeComponent() {
        // TODO: insert component disposal logic here
    }

    @Override
    @NotNull
    public String getComponentName() {
        return "ProjectComponent";
    }

    @Override
    public void projectOpened() {
        // called when project is opened
    }

    @Override
    public void projectClosed() {
        if (this.isClient()) {
            // TODO this.client.closeConnection();
        }
        if (this.isHosting()) {
            // TODO this.server.closeConnection();
        }
    }

    public boolean isHosting() {
        return server != null;
    }

    public boolean isClient() {
        return client != null;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    @NotNull
    public static ProjectComponent getPrPiProjComp(@Nullable Project project) throws NullPointerException {
        if (project == null) {
            project = ApplicationComponent.getCurrentProject();
        }
        if (project == null) {
            throw new NullPointerException("No project found.");
        }
        ProjectComponent component = project.getComponent(ProjectComponent.class);
        if (component == null) {
            throw new NullPointerException("No component found.");
        }
        return component;
    }

    public void sendMessage(Message msg) {
        try {
            if (isClient()) {
                client.sendMessageToServer(msg);
            } else if (isHosting()) {
                server.sendMessageToClients(msg);
            } else {
                logger.error("Try to send a message but this project is not hosting or is not a client !");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public Project getProject() {
        return project;
    }
}
