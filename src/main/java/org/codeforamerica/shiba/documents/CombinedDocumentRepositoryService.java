package org.codeforamerica.shiba.documents;

import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
public class CombinedDocumentRepositoryService {

  private final S3DocumentRepositoryService s3DocumentRepositoryService;
  private final AzureDocumentRepositoryService azureDocumentRepositoryService;

  public CombinedDocumentRepositoryService(S3DocumentRepositoryService s3DocumentRepositoryService,
      AzureDocumentRepositoryService azureDocumentRepositoryService) {
    this.s3DocumentRepositoryService = s3DocumentRepositoryService;
    this.azureDocumentRepositoryService = azureDocumentRepositoryService;
  }

  public byte[] getFromAzureWithFallbackToS3(String filepath) {
    log.info("Checking for filepath " + filepath + " in Azure.");
    byte[] content = azureDocumentRepositoryService.get(filepath);
    if (null == content) {
      log.info("File at filepath " + filepath + " cannot be found in Azure. Now checking S3.");
      content = s3DocumentRepositoryService.get(filepath);
      if (null == content) {
        log.error("File at filepath " + filepath + " cannot be found in Azure or S3.");
      }
    }
    return content;
  }

  public void upload(String filepath, MultipartFile file) throws InterruptedException, IOException {
    azureDocumentRepositoryService.upload(filepath, file);
  }

  public void upload(String filepath, String fileContent) throws InterruptedException, IOException {
    azureDocumentRepositoryService.upload(filepath, fileContent);
  }

  public void delete(String filepath) {
    azureDocumentRepositoryService.delete(filepath);
  }
}
