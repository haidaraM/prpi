package com.prpi.network.communication;

import java.util.Arrays;

public class FileContent extends Transaction {

    /**
     * The file ID (ID of an File object) attached of this FileContent
     */
    private String fileId;

    /**
     * The part of the content of the File
     */
    private byte[] content;

    /**
     * Size of the content array
     */
    private int sizeContent;

    /**
     * The order of this part in all FileContent composed the File
     */
    private int order;

    /**
     * True if this FileContent is the last of all parts of the content of the File
     */
    private boolean lastContent;

    FileContent(String fileId, byte[] content, int sizeContent, int order, boolean lastContent, TransactionType transactionType) {
        super(FileContent.class, transactionType);
        this.fileId = fileId;
        this.content = content;
        this.order = order;
        this.lastContent = lastContent;
        this.json = Transaction.gson.toJson(this);
    }

    /**
     * @return true if this FileContent is the last of all the content of the File
     */
    public boolean isLastContent() {
        return lastContent;
    }

    /**
     * @return the order of this FileContent in all parts of the content of the final file
     */
    public int getOrder() {
        return order;
    }

    /**
     * @return the ID of the File attached (ID of an File object)
     */
    public String getFileId() {
        return fileId;
    }

    /**
     * @return the part of the content of the final file
     */
    public byte[] getContent() {
        return content;
    }

    /**
     * @return the real size of the content data array
     */
    public int getSizeContent() {
        return sizeContent;
    }

    @Override
    public String toString() {
        return "FileContent{" +
                "fileId='" + fileId + '\'' +
                //", content=" + Arrays.toString(content) +
                ", sizeContent=" + sizeContent +
                ", order=" + order +
                ", lastContent=" + lastContent +
                "} " + super.toString();
    }
}
