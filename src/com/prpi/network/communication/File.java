package com.prpi.network.communication;

class File extends Message<String> {

    private String fileName;
    private String pathInProject;
    private int fileSize;

    public File(String fileName, String pathInProject, int fileSize, String content) {
        super(content);
        this.fileName = fileName;
        this.pathInProject = pathInProject;
        this.fileSize = fileSize;
    }
}
