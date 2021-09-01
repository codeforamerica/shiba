package org.codeforamerica.shiba;

import java.io.IOException;
import org.apache.commons.lang.NotImplementedException;
import org.codeforamerica.shiba.documents.DocumentRepositoryService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.web.multipart.MultipartFile;

@TestConfiguration
public class DocumentRepositoryServiceTestConfig {

  @Bean
  @Primary
  public DocumentRepositoryService combinedDocumentRepositoryService() {
    return new LocalFilesystemDocumentRepositoryService();
  }

  public static class LocalFilesystemDocumentRepositoryService implements
      DocumentRepositoryService {

    @Override
    public byte[] get(String filepath) {
      return new byte[0];
    }

    @Override
    public void upload(String filepath, MultipartFile file)
        throws IOException, InterruptedException {
      throw new NotImplementedException();
    }

    @Override
    public void upload(String filepath, String fileContent)
        throws IOException, InterruptedException {
      throw new NotImplementedException();
    }

    @Override
    public void delete(String filepath) {
      throw new NotImplementedException();
    }
  }
}
