package com.prpi.actions.tests;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.prpi.actions.DocumentActionsHelper;

public class InsertStringAction extends AnAction {
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

        String inputString = Messages.showInputDialog(project, "String to insert", "String To Insert",
                Messages.getQuestionIcon());
        if (inputString == null) {
            return;
        }

        inputString = Messages.showInputDialog(project, "Start offset", "Start Offset",
                Messages.getQuestionIcon());

        if (inputString == null) {
            return;
        }

        int offset = Integer.valueOf(inputString);

        DocumentActionsHelper.insertStringInDocument(project,document,inputString,offset);
    }
}
