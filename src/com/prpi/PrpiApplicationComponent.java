package com.prpi;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.prpi.document.PrpiDocumentListenner;
import org.jetbrains.annotations.NotNull;

public class PrpiApplicationComponent implements ApplicationComponent {
    public PrpiApplicationComponent() {
    }

    @Override
    public void initComponent() {
        // TODO: insert component initialization logic here
        privateSetupDocuementListener();
    }

    @Override
    public void disposeComponent() {
        // TODO: insert component disposal logic here
    }

    @Override
    @NotNull
    public String getComponentName() {
        return "PrpiApplicationComponent";
    }


    void privateSetupDocuementListener(){
        VirtualFileManager.getInstance().addVirtualFileListener(new PrpiDocumentListenner());
    }
}
