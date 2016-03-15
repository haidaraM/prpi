package com.prpi;

import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.prpi.filesystem.PrPiVirtualFileListener;
import org.jetbrains.annotations.NotNull;


/**
 * For more documentation, see the followings links :
 *  - http://www.jetbrains.org/intellij/sdk/docs/basics/plugin_structure/plugin_components.html?search=comp
 *  - https://upsource.jetbrains.com/idea-ce/file/idea-ce-1731d054af4ca27aa827c03929e27eeb0e6a8366/platform/core-api/src/com/intellij/openapi/components/ApplicationComponent.java
 */
public class PrPiApplicationComponent implements ApplicationComponent {



    public PrPiApplicationComponent() {
    }

    @Override
    public void initComponent() {
        // TODO: insert component initialization logic here
        setupDocuementListener();
    }

    @Override
    public void disposeComponent() {
        // TODO: insert component disposal logic here
    }

    @Override
    @NotNull
    public String getComponentName() {
        return "PrPiApplicationComponent";
    }


    private void setupDocuementListener(){


        // TODO : maybe setupListenner asynchronously to avoid delays during application launch
        VirtualFileManager.getInstance().addVirtualFileListener(new PrPiVirtualFileListener());
    }
}
