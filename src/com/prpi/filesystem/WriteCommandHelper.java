package com.prpi.filesystem;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;

/**
 * This class contains some static methods to deal with write actions on documents by automatically use CommandProcessor
 */
public class WriteCommandHelper {


    /**
     * Replace the content of the document by newContent
     *
     * @param document the document to modify
     * @param newContent the new tewt of the document
     */
    public static void replaceDocumentContent(Project project, Document document, String newContent) {
        runWriteAction(project, () -> document.setText(newContent), "replaceDocumentContent");

    }

    /**
     * Insert a text in the document
     * @param project the current project
     * @param document the document to modify
     * @param charSequence the text to insert
     * @param offset the offset to insert the text at.
     */
    public static void insertStringInDocument(Project project, Document document, CharSequence charSequence, int offset) {
        runWriteAction(project, () -> document.insertString(offset, charSequence), "insertStringInDocument");
    }

    /**
     * Delete text in the document
     * @param project the current project
     * @param document the document to modify
     * @param startOffset the start offset of the range to delete.
     * @param endOffset the end offset of the range to delete.
     */
    public static void deleteStringIndocument(Project project, Document document, int startOffset, int endOffset) {
        runWriteAction(project, () -> document.deleteString(startOffset, endOffset), "deleteStringIndocument");
    }


    /**
     *
     * @param project the current project
     * @param runnable the runnable to run
     * @param commandeName a name for the command.
     */
    private static void runWriteAction(Project project, Runnable runnable, String commandeName) {
        ApplicationManager.getApplication().invokeLater(
                () -> CommandProcessor.getInstance().executeCommand(project,
                        () -> ApplicationManager.getApplication().runWriteAction(runnable), commandeName,
                        null));

    }
}
