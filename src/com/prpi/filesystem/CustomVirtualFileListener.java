package com.prpi.filesystem;


import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.*;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * Listen virtual file changes
 */
public class CustomVirtualFileListener implements com.intellij.openapi.vfs.VirtualFileListener {

    private static Logger logger = Logger.getLogger(CustomVirtualFileListener.class);

    //static {
    //    logger.setLevel(Level.TRACE);
    //}


    @Override
    public void propertyChanged(@NotNull VirtualFilePropertyEvent event) {
    }

    @Override
    public void contentsChanged(@NotNull VirtualFileEvent event) {

        // Just for testing, we only handle event is the user explicitly save the file. This avoid project files to be processed

        if (event.isFromSave()) {
            Document document = FileDocumentManager.getInstance().getDocument(event.getFile());
            if(document != null){

                DataContext dataContext = (DataContext) DataManager.getInstance().getDataContextFromFocus().getResultSync();

                // get the editor
                Editor editor = (Editor) DataKeys.EDITOR.getData(dataContext);

                // get logical Position
                LogicalPosition logicalPosition = editor.getCaretModel().getLogicalPosition();
                Caret caret = editor.getCaretModel().addCaret(new VisualPosition(logicalPosition.line + 1, logicalPosition.column));

            }
        }

    }

    @Override
    public void fileCreated(@NotNull VirtualFileEvent event) {

    }

    @Override
    public void fileDeleted(@NotNull VirtualFileEvent event) {

    }

    @Override
    public void fileMoved(@NotNull VirtualFileMoveEvent event) {

    }

    @Override
    public void fileCopied(@NotNull VirtualFileCopyEvent event) {

    }

    @Override
    public void beforePropertyChange(@NotNull VirtualFilePropertyEvent event) {

    }

    @Override
    public void beforeContentsChange(@NotNull VirtualFileEvent event) {

    }

    @Override
    public void beforeFileDeletion(@NotNull VirtualFileEvent event) {

    }

    @Override
    public void beforeFileMovement(@NotNull VirtualFileMoveEvent event) {

    }
}
