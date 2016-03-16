package com.prpi.filesystem;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;


/**
 * Listen document changes
 */
public class PrpiDocumentListener implements DocumentListener {
    @Override
    public void beforeDocumentChange(DocumentEvent event) {

    }

    @Override
    public void documentChanged(DocumentEvent event) {

        Project project = ProjectManager.getInstance().getOpenProjects()[0];
        System.out.println(project.getName());

        DataContext dataContext = (DataContext) DataManager.getInstance().getDataContext();

        // get the editor
        Editor editor = (Editor) DataKeys.EDITOR.getData(dataContext);

        // get logical Position
        LogicalPosition logicalPosition = editor.getCaretModel().getLogicalPosition();

        // print line number : 0-based format => +1
        System.out.println(String.format("Current line number : %d", logicalPosition.line+1));
        System.out.println(String.format("Current column number : %d", logicalPosition.column+1));
    }
}
