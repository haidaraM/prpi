package com.prpi.filesystem;


import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.*;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * Listen document changes
 */
public class PrPiVirtualFileListener implements VirtualFileListener {

    private static Logger logger = Logger.getLogger(PrPiVirtualFileListener.class);

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

                // print line number : 0-based format => +1
                logger.trace(String.format("Current line number : %d", logicalPosition.line+1));
                logger.trace(String.format("Current column number : : %d", logicalPosition.column+1));
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