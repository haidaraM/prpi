package com.prpi.filesystem;


import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.prpi.ProjectComponent;
import com.prpi.actions.DocumentActionsHelper;
import org.jetbrains.annotations.Nullable;


public class HeartBeat {

    /**
     * Line which was edited
     */
    private int line;

    /**
     * Column which was edited
     */
    private int column;

    /**
     * Absolute path
     */
    private String filePath;

    /**
     * The name of the file (with extension)
     */
    private String fileName;


    /**
     * Represents the old deleted character if the user delete a character. Must be empty if it's a insert heart beart
     */
    private CharSequence oldFragment;

    /**
     * Represents the new inserted character if the user insert a character. Must be empty if it's a delete heart beat
     */
    private CharSequence newFragment;

    /**
     * Carret offset in the editor
     */
    private int caretOffset;


    public HeartBeat(int line, int column, String filePath, CharSequence oldFragment, CharSequence newFragment, int caretOffset, String fileName) {
        this.line = line;
        this.column = column;
        this.filePath = filePath;
        this.oldFragment = oldFragment;
        this.newFragment = newFragment;
        this.caretOffset = caretOffset;
        this.fileName = fileName;
    }


    public int getLine() {
        return line;
    }


    public int getColumn() {
        return column;
    }



    public String getFilePath() {
        return filePath;
    }



    public CharSequence getOldFragment() {
        return oldFragment;
    }



    public CharSequence getNewFragment() {
        return newFragment;
    }


    /**
     * Check if this heart beat is a insert heart beat
     *
     * @return true if insert, false otherwise.
     */
    public boolean isInsertHeartBeat() {
        return newFragment.length() != 0;
    }

    public int getCaretOffset() {
        return caretOffset;
    }

    public String getFileName() {
        return fileName;
    }

    /**
     * Get the document which correspond to the filePath
     *
     * @return document
     */
    @Nullable
    public Document getDocument() {

        VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByPath(this.filePath);
        if (virtualFile == null)
            return null;

        return FileDocumentManager.getInstance().getDocument(virtualFile);
    }


}
