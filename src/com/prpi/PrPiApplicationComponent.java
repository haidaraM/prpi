package com.prpi;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.prpi.filesystem.PrPiVirtualFileListener;
import com.prpi.filesystem.PrpiDocumentListener;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * For more documentation, see the followings links :
 * - http://www.jetbrains.org/intellij/sdk/docs/basics/plugin_structure/plugin_components.html?search=comp
 * - https://upsource.jetbrains.com/idea-ce/file/idea-ce-1731d054af4ca27aa827c03929e27eeb0e6a8366/platform/core-api/src/com/intellij/openapi/components/ApplicationComponent.java
 */
public class PrPiApplicationComponent implements ApplicationComponent {

    private static final Logger logger = Logger.getLogger(PrPiApplicationComponent.class);

    public PrPiApplicationComponent() {
    }

    @Override
    public void initComponent() {

        // Set the log4j configuration file
        DOMConfigurator.configure(PrPiApplicationComponent.class.getClassLoader().getResource("log4j.xml"));

        logger.trace("Init component");

        // TODO: insert component initialization logic here
        setupDocuementListener();

        logger.trace("End of component init");
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


    private void setupDocuementListener() {

        ApplicationManager.getApplication().invokeLater(() -> {
            VirtualFileManager.getInstance().addVirtualFileListener(new PrPiVirtualFileListener());
            EditorFactory.getInstance().getEventMulticaster().addDocumentListener(new PrpiDocumentListener());
        });
    }

    @NotNull
    public static PrPiApplicationComponent getPrPiAppComp(@Nullable Project project) throws NullPointerException {
        if (project == null) {
            project = PrPiApplicationComponent.getCurrentProject();
        }
        if (project == null) {
            throw new NullPointerException("No project found.");
        }
        PrPiApplicationComponent component = project.getComponent(PrPiApplicationComponent.class);
        if (component == null) {
            throw new NullPointerException("No component found.");
        }
        return component;
    }


    @Nullable
    public static Project getCurrentProject() {
        DataContext dataContext = DataManager.getInstance().getDataContext();
        Project project = null;
        try {
            project = PlatformDataKeys.PROJECT.getData(dataContext);
        } catch (NoClassDefFoundError e) {
            logger.warn(e.getMessage());

            try {
                project = DataKeys.PROJECT.getData(dataContext);
            } catch (NoClassDefFoundError ex) {
                logger.warn(ex.getMessage());
            }
        }
        return project;
    }
}
