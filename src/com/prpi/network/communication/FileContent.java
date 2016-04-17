package com.prpi.network.communication;

import com.google.gson.Gson;

/**
 * Created by Pierre on 17/04/2016.
 */
public class FileContent extends Transaction {

    private String fileId;

    private byte[] content;

    private int sizeContent;

    private int order;

    private boolean lastContent;

    FileContent(String fileId, byte[] content, int sizeContent, int order, boolean lastContent, PrPiTransaction transactionType) {
        super(FileContent.class, transactionType);
        this.fileId = fileId;
        this.content = content;
        this.order = order;
        this.lastContent = lastContent;
        this.json = gson.toJson(this);
    }

    public boolean isLastContent() {
        return lastContent;
    }

    public int getOrder() {
        return order;
    }

    public String getFileId() {
        return fileId;
    }
}
