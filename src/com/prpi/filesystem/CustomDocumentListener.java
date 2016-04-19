package com.prpi.filesystem;

import com.intellij.openapi.editor.*;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.prpi.ApplicationComponent;
import org.apache.log4j.Logger;


/**
 * Listen document changes
 */
public class CustomDocumentListener implements com.intellij.openapi.editor.event.DocumentListener {

    private static final Logger logger = Logger.getLogger(CustomDocumentListener.class);

   /* static {
        logger.setLevel(Level.TRACE);
    } */

    @Override
    public void beforeDocumentChange(DocumentEvent event) {
    }

    @Override
    public void documentChanged(DocumentEvent event) {

        Project project = ApplicationComponent.getCurrentProject();

        // get the editor
        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();

        // get logical Position
        LogicalPosition logicalPosition = editor.getCaretModel().getLogicalPosition();

        // print line number : 0-based format => +1
        logger.trace(String.format("Line number : %d", logicalPosition.line + 1));
        logger.trace(String.format("Column number : %d", logicalPosition.column + 1));

    }
}
