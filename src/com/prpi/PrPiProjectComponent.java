package com.prpi;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.prpi.network.client.Client;
import com.prpi.network.server.Server;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class PrPiProjectComponent implements ProjectComponent {

    private Server server;
    private Client client;
    private Project project;

    public PrPiProjectComponent(Project project) {
        this.project = project;
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
        return "PrPiProjectComponent";
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
    public static PrPiProjectComponent getPrPiProjComp(@Nullable Project project) throws NullPointerException {
        if (project == null) {
            project = PrPiApplicationComponent.getCurrentProject();
        }
        if (project == null) {
            throw new NullPointerException("No project found.");
        }
        PrPiProjectComponent component = project.getComponent(PrPiProjectComponent.class);
        if (component == null) {
            throw new NullPointerException("No component found.");
        }
        return component;
    }
}
