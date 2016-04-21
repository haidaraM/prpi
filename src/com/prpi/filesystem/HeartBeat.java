package com.prpi.filesystem;


import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.prpi.ProjectComponent;
import com.prpi.actions.DocumentActionsHelper;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.Nullable;


public class HeartBeat {


    private static final Logger logger = Logger.getLogger(HeartBeat.class);

    static {
        logger.setLevel(Level.TRACE);
    }

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
    private String oldFragment;

    /**
     * Represents the new inserted character if the user insert a character. Must be empty if it's a delete heart beat
     */
    private String newFragment;

    /**
     * Carret offset in the editor
     */
    private int caretOffset;


    public HeartBeat(int line, int column, String filePath, String oldFragment, String newFragment, int caretOffset, String fileName) {
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


    public String getOldFragment() {
        return oldFragment;
    }


    public String getNewFragment() {
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
     * Get the document which corresponds to the filePath
     *
     * @return document
     */
    @Nullable
    public Document getDocument() {

        VirtualFile virtualFile = getVirtualFile();
        if (virtualFile == null) {
            return null;
        } else {
            return FileDocumentManager.getInstance().getDocument(virtualFile);
        }
    }

    /**
     * Get virtual file which corresponds to the filepath
     *
     * @return virtual file
     */
    @Nullable
    public VirtualFile getVirtualFile() {
        Project project = ProjectComponent.getInstance().getProject();

        PsiFile[] psiFiles = FilenameIndex.getFilesByName(project, this.fileName,
                GlobalSearchScope.projectScope(project));
        if (psiFiles.length == 0) {
            logger.trace(String.format("File '%s' not found in project scope", this.fileName));
            return null;
        } else {

            // I Suppose that there are not more than two files whith the same name.
            return psiFiles[0].getVirtualFile();
        }
    }

    @Override
    public String toString() {
        return "HeartBeat{" +
                "line=" + line +
                ", column=" + column +
                ", filePath='" + filePath + '\'' +
                ", fileName='" + fileName + '\'' +
                ", oldFragment=" + oldFragment +
                ", newFragment=" + newFragment +
                ", caretOffset=" + caretOffset +
                '}';
    }
}
