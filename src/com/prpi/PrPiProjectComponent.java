package com.prpi;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.prpi.network.PrPiClient;
import com.prpi.network.PrPiServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class PrPiProjectComponent implements ProjectComponent {

    private PrPiServer serverThread;
    private PrPiClient clientThread;
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
            this.clientThread.closeConnection();
        }
        if (this.isHosting()) {
            this.serverThread.closeConnection();
        }
    }

    public boolean isHosting() {
        return serverThread != null;
    }

    public boolean isClient() {
        return clientThread != null;
    }

    public PrPiClient getClientThread() {
        return clientThread;
    }

    public void setClientThread(PrPiClient clientThread) {
        this.clientThread = clientThread;
    }

    public PrPiServer getServerThread() {
        return serverThread;
    }

    public void setServerThread(PrPiServer serverThread) {
        this.serverThread = serverThread;
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
