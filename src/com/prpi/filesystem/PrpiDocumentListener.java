package com.prpi.filesystem;

import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;


/**
 * Listen document changes
 */
public class PrpiDocumentListener implements DocumentListener {

    private static final Logger logger = Logger.getLogger(PrpiDocumentListener.class);

   /* static {
        logger.setLevel(Level.TRACE);
    } */

    @Override
    public void beforeDocumentChange(DocumentEvent event) {

    }

    @Override
    public void documentChanged(DocumentEvent event) {

        Project project = ProjectManager.getInstance().getOpenProjects()[0];

        // get the editor
        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();

        // get logical Position
        LogicalPosition logicalPosition = editor.getCaretModel().getLogicalPosition();

        // print line number : 0-based format => +1
        logger.trace(String.format("Current line number : %d", logicalPosition.line + 1));
        logger.trace(String.format("Current column number : %d", logicalPosition.column + 1));

    }
}
