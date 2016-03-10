package com.prpi.document;


import com.intellij.openapi.vfs.*;
import org.jetbrains.annotations.NotNull;

/**
 * Listen document changes
 */
public class PrpiDocumentListenner implements VirtualFileListener{



    @Override
    public void propertyChanged(@NotNull VirtualFilePropertyEvent event) {
    }

    @Override
    public void contentsChanged(@NotNull VirtualFileEvent event) {
        System.out.println("contentschanged : " + event.getFileName());
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
