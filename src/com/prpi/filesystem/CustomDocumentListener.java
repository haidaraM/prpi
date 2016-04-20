package com.prpi.filesystem;

import com.intellij.openapi.editor.*;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.prpi.ProjectComponent;
import com.prpi.actions.DocumentActionsHelper;
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

    private String id;

    /**
     * A dummy just for equals to work as I want
     *
     * @param id
     */
    public CustomDocumentListener(String id) {
        this.id = id;
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


            LogicalPosition logicalPosition = editor.getCaretModel().getLogicalPosition();
            logger.trace(String.format("File name : %s", virtualFile.getName()));
            logger.trace(String.format("Line number : %d", logicalPosition.line + 1));
            logger.trace(String.format("Column number : %d", logicalPosition.column + 1));
            logger.trace(String.format("New fragment : '%s'", event.getNewFragment()));
            logger.trace(String.format("Old fragment : '%s'", event.getOldFragment()));
            logger.trace(String.format("Caret offset : %d", editor.getCaretModel().getOffset()));

            //  DocumentActionsHelper.hightLightLineInSelectedEditor(event.getDocument(),logicalPosition.line);

            HeartBeat heartBeat = new HeartBeat(logicalPosition.line, logicalPosition.column, virtualFile.getPath(),
                    event.getOldFragment(), event.getNewFragment(), editor.getCaretModel().getOffset(),
                    virtualFile.getName());
            //DocumentActionsHelper.createGuardedBlock(event.getDocument(),logicalPosition.line);

            //DocumentActionsHelper.insertStringInDocument(project,event.getDocument(),"p",editor.getCaretModel().getOffset()+1);


            ProjectComponent.getInstance().sendMessage(new Message<HeartBeat>(heartBeat, Transaction.TransactionType.HEART_BEAT));


        } catch (NullPointerException ignored) {
            // Some changes seem not to be related on virtual files. So sometimes we have NullPointerException.
            // But all changes made by user are properly handled
        }

    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof CustomDocumentListener)) return false;

        CustomDocumentListener customDocumentListener = (CustomDocumentListener) obj;

        return this.id.equals(customDocumentListener.id);
    }
}
