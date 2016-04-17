package com.prpi.network.communication;

import com.google.gson.Gson;
import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

class File extends Transaction {

    /**
     * The name of the file (with extension)
     */
    private String fileName;

    /**
     * The path to the file in the project (root is the root of the project)
     */
    private String pathInProject;

    /**
     * The size of the file
     */
    private int fileSize;

    /**
     * The file id.
     */
    private String id;

    /**
     * The absolute path of the root project to read and write file
     */
    private transient String pathOfProjectRoot;

    // TODO : FileContent comparator (order)
    private transient List<FileContent> contents = new ArrayList<>();

    private transient int lastContentOrder = -1;

    private transient boolean complete = false;

    private static transient final Logger logger = Logger.getLogger(File.class);

    /**
     * The json builder
     */
    private transient static final Gson gson = new Gson();

    public File(Path file, Path projectRoot, PrPiTransaction transactionType) {
        super(File.class, transactionType);
        this.fileName = file.getFileName().toString();
        this.pathInProject = getPathToFileInProjectRoot(file, projectRoot);
        this.fileSize = File.getFileSize(file);
        this.pathOfProjectRoot = projectRoot.toString();
        this.id = UUID.randomUUID().toString();
        this.json = gson.toJson(this);
    }

    File(String fileName, String pathInProject, int fileSize, PrPiTransaction transactionType) {
        super(File.class, transactionType);
        this.fileName = fileName;
        this.pathInProject = pathInProject;
        this.fileSize = fileSize;
        this.id = UUID.randomUUID().toString();
        this.json = gson.toJson(this);
    }


    private static String encodeFileData(byte[] fileData) {
        return Base64.getEncoder().encodeToString(fileData);
    }

    private static byte[] decodeFileData(String fileDataEncodedBase64) {
        return Base64.getDecoder().decode(fileDataEncodedBase64);
    }

    private static int getFileSize(Path pathToFile) {
        long size;
        try {
            size = Files.size(pathToFile);
            if (size > Integer.MAX_VALUE)
                return -1;
            return (int) size;
        } catch (IOException e) {
            logger.error("Could not estimate file size", e);
            return -1;
        }
    }

    private static String getPathToFileInProjectRoot(Path pathToFile, Path projectBasePath) {
        if (pathToFile.toString().startsWith(projectBasePath.toString()))
            return pathToFile.toString().substring(projectBasePath.toString().length());

        return pathToFile.toString();
    }

    public String getId() {
        return id;
    }

    public int getSize() {
        return fileSize;
    }

    public String getFileName() {
        return fileName;
    }

    public void addFileContent(FileContent content) {
        contents.add(content);

        if (content.isLastContent()) {
            lastContentOrder = content.getOrder();
        }

        if (lastContentOrder != -1 && (contents.size() - 1) == lastContentOrder) {
            complete = true;
        }
    }

    public boolean isComplete() {
        return complete;
    }
}
