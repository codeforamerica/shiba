package org.codeforamerica.shiba;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.commons.io.FileUtils;
import org.codeforamerica.shiba.documents.DocumentRepository;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.web.multipart.MultipartFile;

@TestConfiguration
public class DocumentRepositoryTestConfig {

  @Bean
  @Primary
  public DocumentRepository combinedDocumentRepositoryService() throws IOException {
    return new LocalFilesystemDocumentRepository();
  }

  /*
   * An implementation of DocumentRepositoryService that saves files to the local filesystem
   */
  public static class LocalFilesystemDocumentRepository implements DocumentRepository {

    private final Path tempDirectory;

    public LocalFilesystemDocumentRepository() throws IOException {
      tempDirectory = Files.createTempDirectory("");
    }

    @Override
    public byte[] get(String filepath) {
      try {
        var fileToRead = new File(tempDirectory.toFile(), filepath);
        return FileUtils.readFileToByteArray(fileToRead);
      } catch (Exception e) {
        return new byte[0];
      }
    }

    @Override
    public void upload(String filepath, MultipartFile sourceFile)
        throws IOException, InterruptedException {
      File targetFile = new File(tempDirectory.toFile(), filepath);
      FileUtils.copyInputStreamToFile(sourceFile.getInputStream(), targetFile);
    }

    @Override
    public void upload(String filepath, String fileContent)
        throws IOException, InterruptedException {
      File targetFile = new File(tempDirectory.toFile(), filepath);
      FileUtils.write(targetFile, fileContent, StandardCharsets.UTF_8);
    }

    @Override
    public void delete(String filepath) {
      File fileToDelete = new File(tempDirectory.toFile(), filepath);
      if (!fileToDelete.delete()) {
        throw new RuntimeException("could not delete file " + fileToDelete);
      }
    }
  }
}
