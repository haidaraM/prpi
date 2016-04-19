package com.prpi.filesystem;

import com.intellij.openapi.editor.*;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.prpi.ProjectComponent;
import com.prpi.network.communication.Message;
import com.prpi.network.communication.Transaction;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;


/**
 * Listen document changes
 */
public class CustomDocumentListener implements com.intellij.openapi.editor.event.DocumentListener {

    private static final Logger logger = Logger.getLogger(CustomDocumentListener.class);

    static {
        logger.setLevel(Level.TRACE);
    }

    @Override
    public void beforeDocumentChange(DocumentEvent event) {
    }

    @Override
    public void documentChanged(DocumentEvent event) {

        Project project = ProjectComponent.getInstance().getProject();

        // get the editor
        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();

        PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(event.getDocument());


        try {
            VirtualFile virtualFile = psiFile.getVirtualFile();


            logger.trace(virtualFile.getPath());
            LogicalPosition logicalPosition = editor.getCaretModel().getLogicalPosition();
            logger.trace(String.format("Line number : %d", logicalPosition.line + 1));
            logger.trace(String.format("Column number : %d", logicalPosition.column + 1));

            logger.trace(event.getNewFragment());
            logger.trace(event.getOldFragment());

            HeartBeat heartBeat = new HeartBeat(logicalPosition.line, logicalPosition.column, virtualFile.getName(),
                    event.getOldFragment(), event.getNewFragment(), editor.getCaretModel().getOffset());

            //WriteCommandHelper.insertStringInDocument(project,event.getDocument(),"p",editor.getCaretModel().getOffset()+1);

            ProjectComponent.getInstance().sendMessage(
                    new Message<>(heartBeat, Transaction.TransactionType.SIMPLE_MESSAGE)
            );

        } catch (NullPointerException ignored) {
            // Some changes seem not to be related on virtual files. So sometimes we have NullPointerException.
            // But all changes made by user are properly handled
        }


    }
}
