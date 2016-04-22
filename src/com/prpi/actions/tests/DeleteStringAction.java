package com.prpi.actions.tests;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.prpi.actions.DocumentActionsHelper;

public class DeleteStringAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent e) {
        final Project project = e.getProject();
        if (project == null) {
            return;
        }

        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();

        if (editor == null) {
            return;
        }

        Document document = editor.getDocument();

        VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(document);
        if (virtualFile == null) {
            return;
        }


        String inputString = Messages.showInputDialog(project, "Start offset", "Start Offset",
                Messages.getQuestionIcon());
        if (inputString == null) {
            return;
        }
        int startOffset = Integer.valueOf(inputString);

        inputString = Messages.showInputDialog(project, "Nb characters", "Nb Characters",
                Messages.getQuestionIcon());

        if (inputString == null) {
            return;
        }

        int nbChars = Integer.valueOf(inputString);

        DocumentActionsHelper.deleteStringInDocument(project, document,startOffset,nbChars);

    }
}
