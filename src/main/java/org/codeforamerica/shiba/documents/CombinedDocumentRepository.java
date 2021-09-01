package org.codeforamerica.shiba.documents;

import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
public class CombinedDocumentRepository implements DocumentRepository {

  private final S3DocumentRepository s3DocumentRepository;
  private final AzureDocumentRepository azureDocumentRepository;

  public CombinedDocumentRepository(
      S3DocumentRepository s3DocumentRepository,
      AzureDocumentRepository azureDocumentRepository
  ) {
    this.s3DocumentRepository = s3DocumentRepository;
    this.azureDocumentRepository = azureDocumentRepository;
  }

  @Override
  public byte[] get(String filepath) {
    log.info("Checking for filepath " + filepath + " in Azure.");
    byte[] content = azureDocumentRepository.get(filepath);
    if (null == content) {
      log.info("File at filepath " + filepath + " cannot be found in Azure. Now checking S3.");
      content = s3DocumentRepository.get(filepath);
      if (null == content) {
        log.error("File at filepath " + filepath + " cannot be found in Azure or S3.");
      }
    }
    return content;
  }

  @Override
  public void upload(String filepath, MultipartFile file) throws InterruptedException, IOException {
    azureDocumentRepository.upload(filepath, file);
  }

  @Override
  public void upload(String filepath, String fileContent) throws InterruptedException, IOException {
    azureDocumentRepository.upload(filepath, fileContent);
  }

  @Override
  public void delete(String filepath) {
    azureDocumentRepository.delete(filepath);
  }
}
