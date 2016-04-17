package com.prpi.network.communication;

import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

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
     * The absolute path of the root project to read and write file
     */
    private transient String pathOfProjectRoot;

    private static transient final Logger logger = Logger.getLogger(File.class);

    public File(String fileName, String pathInProject, int fileSize) {
        super(File.class);
        this.fileName = fileName;
        this.pathInProject = pathInProject;
        this.fileSize = fileSize;
        this.pathOfProjectRoot = null;
    }

    public File(String fileName, String pathInProject, int fileSize, String pathOfProjectRoot) {
        this(fileName, pathInProject, fileSize);
        this.pathOfProjectRoot = pathOfProjectRoot;
    }

    @Override
    public String getString(int offset, int length) {

        String result = null;

        Path pathToFile = Paths.get(this.pathOfProjectRoot, this.pathInProject, this.fileName);

        if (Files.isReadable(pathToFile) && !Files.isDirectory(pathToFile) && this.fileSize <= offset + length) {
            try (FileInputStream fileInputStream = new FileInputStream(pathToFile.toFile())) {
                byte[] fileData = new byte[length];
                int readSize = fileInputStream.read(fileData, offset, length);
                if (readSize == length) {
                    result = File.encodeFileData(fileData);
                } else {
                    logger.warn("The read size available in file is less than the lenght given.");
                }
            } catch (IOException e) {
                logger.error("Error when reading byte in file.", e);
            }
        } else {
            logger.error("The file is not readable or is a directory or the length is out of the file !");
        }
        return result;
    }

    @Override
    public int getLength() {
        return  4 * (int)Math.ceil(this.fileSize / 3.0);
    }

    private static String encodeFileData(byte[] fileData) {
        return Base64.getEncoder().encodeToString(fileData);
    }

    private static byte[] decodeFileData(String fileDataEncodedBase64) {
        return Base64.getDecoder().decode(fileDataEncodedBase64);
    }
}
