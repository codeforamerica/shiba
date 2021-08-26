package org.codeforamerica.shiba.documents;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
public class AzureDocumentRepositoryService implements DocumentRepositoryService {

  private final BlobContainerClient containerClient;

  public AzureDocumentRepositoryService() {
    BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
        .connectionString(System.getenv("AZURE_CONNECTION_STRING")).buildClient();
    this.containerClient = blobServiceClient
        .getBlobContainerClient(System.getenv("AZURE_CONTAINER_NAME"));
  }

  @Override
  public byte[] get(String filepath) {
    BlobClient blobClient = containerClient.getBlobClient(filepath);
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

    try {
      blobClient.download(byteArrayOutputStream);
      return byteArrayOutputStream.toByteArray();
    } catch (Exception ex) {
      return null;
    }
  }

  @Override
  public void upload(String filepath, MultipartFile file) throws IOException {
    log.info("Uploading file {} to Azure at filepath {}", file.getOriginalFilename(), filepath);
    try (var inputStream = file.getInputStream()) {
      uploadToAzure(filepath, file.getSize(), inputStream);
    }
  }

  public void upload(String filepath, String fileContent) throws IOException {
    log.info("Uploading file content string to Azure at filepath {}", filepath);
    var fileContentBytes = fileContent.getBytes(StandardCharsets.UTF_8);
    try (var byteArrayInputStream = new ByteArrayInputStream(fileContentBytes)) {
      uploadToAzure(filepath, fileContentBytes.length, byteArrayInputStream);
    }
  }

  private void uploadToAzure(String filepath, long size, InputStream inputStream) {
    BlobClient blobClient = containerClient.getBlobClient(filepath);
    log.info("Uploading to Azure Blob storage as blob:" + blobClient.getBlobUrl());
    blobClient.upload(inputStream, size);
    log.info("finished uploading");
  }

  @Override
  public void delete(String filepath) {
    log.info("Deleting file from Azure at filepath {}", filepath);
    BlobClient blobClient = containerClient.getBlobClient(filepath);
    blobClient.delete();
    log.info("Deleted file {}", filepath);
  }
}
