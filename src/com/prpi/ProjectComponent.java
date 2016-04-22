package com.prpi;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.prpi.filesystem.CustomDocumentListener;
import com.prpi.network.client.Client;
import com.prpi.network.communication.Message;
import com.prpi.network.server.Server;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class ProjectComponent implements com.intellij.openapi.components.ProjectComponent {

    private Server server;
    private Client client;
    private Project project;

    private static ProjectComponent instance;

    private static final Logger logger = Logger.getLogger(ProjectComponent.class);

    private CustomDocumentListener customDocumentListener = null;

    public ProjectComponent(Project project) {
        this.project = project;
        customDocumentListener = new CustomDocumentListener(project, "CustomDocumentListenner");
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
        setupDocumentListener();
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

    public void setupDocumentListener() {

        logger.debug("Adding document listener on project: " + project.getBasePath());
        ApplicationManager.getApplication().invokeLater(() -> {
            //VirtualFileManager.getInstance().addVirtualFileListener(new CustomVirtualFileListener());
            EditorFactory.getInstance().getEventMulticaster().addDocumentListener(customDocumentListener);
        });
    }

    public void removeDocumentListener() {
        logger.debug("Removing document listener on project: " + project.getBasePath());
        EditorFactory.getInstance().getEventMulticaster().removeDocumentListener(customDocumentListener);
    }


    public CustomDocumentListener getDocumentListener() {
        return customDocumentListener;
    }
}
