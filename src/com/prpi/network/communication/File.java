package com.prpi.network.communication;

import com.google.gson.Gson;
import org.apache.log4j.Logger;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

public class File extends Transaction {

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
     * The content of the file
     */
    private transient Map<Integer, FileContent> contents = new TreeMap<>();

    /**
     * The number of the last content (get his order number)
     * Set to -1 when is not known
     */
    private transient int lastContentOrder = -1;

    /**
     * True if all FileContent are added correctly in this File
     */
    private transient boolean complete = false;

    private static transient final Logger logger = Logger.getLogger(File.class);

    /**
     * The json builder
     */
    private transient static final Gson gson = new Gson();

    public File(Path file, Path projectRoot, TransactionType transactionType) {
        super(File.class, transactionType);
        this.fileName = file.getFileName().toString();
        this.pathInProject = getPathToFileInProjectRoot(file, projectRoot);
        this.fileSize = File.getFileSize(file);
        this.id = UUID.randomUUID().toString();
        this.json = Transaction.gson.toJson(this);
    }

    /**
     * Get the size of an file given with is absolute path
     * @param pathToFile absolute path to the file
     * @return the size if is not exceed the int max value / -1 in error cases
     */
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

    /**
     * Give the relative path of the file in the project root
     * @param pathToFile The absolute path to the file
     * @param projectBasePath Tha absolute path to the project root
     * @return String representing the elative path of the file in the project root
     */
    private static String getPathToFileInProjectRoot(Path pathToFile, Path projectBasePath) {
        if (pathToFile.toString().startsWith(projectBasePath.toString()))
            return pathToFile.toString().substring(projectBasePath.toString().length());

        return pathToFile.toString();
    }

    /**
     * Get the ID of this File object (unique)
     * @return String representing the File unique ID
     */
    public String getId() {
        return id;
    }

    /**
     * Get the size of the File represented by this object
     * @return the size of the file
     */
    public int getSize() {
        return fileSize;
    }

    /**
     * Get the name of the file
     * @return String representing the name (with extension) of the File
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Get the relative path of the file in the project
     * @return String representing the relative path (racine = project root) of the file
     */
    public String getPathInProject() {
        return pathInProject;
    }

    /**
     * Add a part of the File content of the file
     * Set the isComplete method to true if all parts are present in the File
     * @param content the part of the file
     */
    public void addFileContent(FileContent content) {

        // Usefull after converting json to object (becasue contents is transient)
        if (contents == null) {
            contents = new TreeMap<>();
        }

        contents.put(content.getOrder(), content);

        if (content.isLastContent()) {
            lastContentOrder = content.getOrder();
        }

        if (lastContentOrder != -1 && (contents.size() - 1) == lastContentOrder) {
            complete = true;
        }
    }

    /**
     * Tell if all parts are in the file and can be write
     * @return True if the file content is complete
     */
    public boolean isComplete() {
        return complete;
    }

    /**
     * Write the file (if is complete) in the relative path of the project root given
     * @param projectRoot the project root
     * @return True is the file is correctly write, false if the file is not complete or if the project root path is not reachable
     * @throws IOException if an I/O exception is throw during the write proccess
     */
    public boolean writeFile(Path projectRoot) throws IOException {
        if (!isComplete()) {
            logger.error("The file to write is not complete");
            return false;
        }

        if (!Files.isReadable(projectRoot)) {
            logger.error("The path of the project root is not readable or reachable : " + projectRoot.toString());
            return false;
        }

        logger.trace("Writing " + fileName + "...");
        // Get the absolute path to the file
        Path filePath = Paths.get(projectRoot.toString(), pathInProject);
        logger.trace("File path: " + filePath);

        // Create all parents directories to the file
        logger.trace("Creating directories of parent: " + filePath.getParent());
        Files.createDirectories(filePath.getParent());

        try (FileOutputStream fileOutputStream = new FileOutputStream(filePath.toFile())) {
            logger.trace("Start writing file (" + (lastContentOrder + 1) + " parts)");

            int offset = 0;
            for (int i = 0; i <= lastContentOrder; i++) {

                FileContent content = contents.get(i);

                // Write the content in the File
                fileOutputStream.write(content.getContent(), offset, content.getSizeContent());
                logger.trace(String.format("Wrote part of %s (%d/%d)", filePath, i+1, (lastContentOrder+1)));

                // Update offset
                offset += content.getSizeContent();
            }
            logger.debug("Completely wrote " + filePath);

            return true;
        }
    }

    @Override
    public String toString() {
        return "File{" +
                "fileName='" + fileName + '\'' +
                ", pathInProject='" + pathInProject + '\'' +
                ", fileSize=" + fileSize +
                ", id='" + id + '\'' +
                ", contents=" + contents +
                ", lastContentOrder=" + lastContentOrder +
                ", complete=" + complete +
                "} " + super.toString();
    }
}
