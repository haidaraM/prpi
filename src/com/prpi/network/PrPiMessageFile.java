package com.prpi.network;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

public class PrPiMessageFile extends PrPiMessage<String> {

    protected String fileName;
    protected String pathInProject;
    protected long fileSize;

    private static final Logger logger = Logger.getLogger(PrPiMessageFile.class);
    private static final Gson gson = new Gson();

    public PrPiMessageFile(Path pathToFile, Path projectBasePath) throws Exception {
        super(PrPiTransaction.FILE_TRANSFERT);

        if (Files.isReadable(pathToFile) && !Files.isDirectory(pathToFile)) {

            this.fileName = pathToFile.getFileName().toString();

            if (projectBasePath != null && pathToFile.toString().startsWith(projectBasePath.toString())) {
                this.pathInProject = pathToFile.toString().substring(projectBasePath.toString().length());
            } else {
                logger.warn("The project base path no corresponding with the path of the file. File path : " + pathToFile.toString());
            }

            try {
                this.fileSize = Files.size(pathToFile);
            } catch (IOException e) {
                logger.error("Impossible to get the size of the file ! File : " + pathToFile.toString());
                throw new IOException("Impossible to get the size of the file ! File : " + pathToFile.toString());
            }

            byte[] fileData = Files.readAllBytes(pathToFile);
            this.message = Base64.getEncoder().encodeToString(fileData);

        } else {
            logger.error("The file isn't readable or is a directory ! File : " + pathToFile.toString());
            throw new FileNotFoundException("The file isn't readable or is a directory ! File : " + pathToFile.toString());
        }
    }

    public boolean writeFile(Path projectBasePath) {
        Path path = Paths.get(projectBasePath.toString() + this.pathInProject);
        byte[] fileData = Base64.getDecoder().decode(this.message);
        try {
            Files.write(path, fileData);
        } catch (IOException e) {
            logger.error("Impossible to write the new file in this path " + path.toString(), e);
            return false;
        }
        return true;
    }

    public static PrPiMessageFile jsonToPrPiMessageFile(@NotNull String json) throws JsonSyntaxException {
        return gson.fromJson(json, PrPiMessageFile.class);
    }
}
