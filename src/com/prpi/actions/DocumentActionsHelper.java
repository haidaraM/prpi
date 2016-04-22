package com.prpi.actions;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.editor.markup.EffectType;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.JBColor;
import com.prpi.ProjectComponent;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.awt.*;

/**
 * This class contains some static methods to deal with write actions on documents by automatically use CommandProcessor.
 * https://intellij-support.jetbrains.com/hc/en-us/community/posts/206756795-Update-document-on-document-change
 * https://intellij-support.jetbrains.com/hc/en-us/community/posts/206138859-Manipulating-a-Document-without-undo-history
 * https://intellij-support.jetbrains.com/hc/en-us/community/posts/207042355-Read-only-section-in-editor
 * https://intellij-support.jetbrains.com/hc/en-us/community/posts/206115839-When-are-document-inserts-written-to-screen-
 * https://intellij-support.jetbrains.com/hc/en-us/community/posts/206761955-Why-i-need-getLineNumber-int-offset-
 * https://intellij-support.jetbrains.com/hc/en-us/community/posts/206119849-Access-to-method-call-under-caret-instead-of-PsiMethod
 * https://intellij-support.jetbrains.com/hc/en-us/community/posts/206107829-How-to-specific-selection-start-position-and-end-position
 * https://intellij-support.jetbrains.com/hc/en-us/community/posts/206774575-Get-PsiElement-by-file-name
 */
public class DocumentActionsHelper {


    private static final Logger logger = Logger.getLogger(DocumentActionsHelper.class);

    static {
        logger.setLevel(Level.TRACE);
    }

    /**
     * Replace the content of the document by newContent
     *
     * @param document   the document to modify
     * @param newContent the new tewt of the document
     */
    public static void replaceDocumentContent(Project project, Document document, String newContent) {
        runWriteAction(project, () -> document.setText(newContent), "replaceDocumentContent");

    }

    /**
     * Insert a text in the document
     *
     * @param project      the current project
     * @param document     the document to modify
     * @param charSequence the text to insert
     * @param offset       the offset to insert the text at.
     */
    public static void insertStringInDocument(Project project, Document document, CharSequence charSequence, int offset) {
        runWriteAction(project, () -> document.insertString(offset, charSequence), "insertStringInDocument");
    }

    /**
     * Delete text in the document
     *
     * @param project     the current project
     * @param document    the document to modify
     * @param startOffset the start offset of the range to delete.
     */
    public static void deleteStringInDocument(Project project, Document document, int startOffset, int numberOfCharacters) {
        runWriteAction(project, () -> document.deleteString(startOffset, startOffset+numberOfCharacters), "deleteStringInDocument");
    }


    /**
     * @param project      the current project
     * @param runnable     the runnable to run
     * @param commandeName a name for the command.
     */
    private static void runWriteAction(Project project, Runnable runnable, String commandeName) {
        ApplicationManager.getApplication().invokeLater(
                () -> CommandProcessor.getInstance().executeCommand(project,
                        () -> ApplicationManager.getApplication().runWriteAction(runnable), commandeName,
                        null));
    }

    /**
     * Block a line in the given document
     *
     * @param document   the document in which you want to block a line
     * @param lineNumber the line number to block
     * @return the marker instance
     */
    public static RangeMarker createGuardedBlock(Document document, int lineNumber) {
        int startOffset = StringUtil.lineColToOffset(document.getCharsSequence(), lineNumber, 0);
        int endOffset = StringUtil.lineColToOffset(document.getCharsSequence(), lineNumber + 1, 0);

        return document.createGuardedBlock(startOffset, endOffset);
    }

    /**
     * Remove the guarded block
     *
     * @param document    the document in which you want to remo
     * @param rangeMarker the marker to remove
     */
    public static void removeGuardedBlock(Document document, RangeMarker rangeMarker) {
        document.removeGuardedBlock(rangeMarker);
    }

    /**
     *
     */
    public static void hightLightLineInSelectedEditor(int lineNumber) {
        Editor editor = FileEditorManager.getInstance(
                ProjectComponent.getInstance().getProject()).getSelectedTextEditor();

        if (editor != null) {
            // TODO : change colors
            TextAttributes textAttributes = new TextAttributes(JBColor.BLACK, Color.lightGray, Color.blue,
                    EffectType.BOXED, Font.BOLD);
            editor.getMarkupModel().addLineHighlighter(lineNumber, HighlighterLayer.LAST, textAttributes);
        }
    }
}
