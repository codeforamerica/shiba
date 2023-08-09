package org.codeforamerica.shiba.documents;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
public class AzureDocumentRepository implements DocumentRepository {

  private BlobContainerClient containerClient;

  public AzureDocumentRepository(
      @Value("${AZURE_CONNECTION_STRING:}") String connectionString,
      @Value("${AZURE_CONTAINER_NAME:}") String containerName
  ) {
    if (!connectionString.equals("") && !containerName.equals("")) {
      var blobServiceClient = new BlobServiceClientBuilder()
          .connectionString(connectionString)
          .buildClient();
      this.containerClient = blobServiceClient.getBlobContainerClient(containerName);
    }
  }

  public byte[] get(String filepath) {
    BlobClient blobClient = containerClient.getBlobClient(filepath);
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

    try {
      log.info("Checking for filepath " + filepath + " in Azure.");
      blobClient.downloadStream(byteArrayOutputStream);
      return byteArrayOutputStream.toByteArray();
    } catch (Exception ex) {
      log.error("File at filepath " + filepath + " cannot be found in Azure.");
      return null;
    }
  }

  public void upload(String filepath, MultipartFile file) throws IOException {
    log.info("Uploading file to Azure at filepath {}", filepath);
    try (var inputStream = file.getInputStream()) {
      uploadToAzure(filepath, file.getSize(), inputStream);
    }
  }

  public void upload(String filepath, String fileContent) throws IOException {
    log.info("Uploading file to Azure at filepath {}", filepath);
    var fileContentBytes = fileContent.getBytes(StandardCharsets.UTF_8);
    try (var byteArrayInputStream = new ByteArrayInputStream(fileContentBytes)) {
      uploadToAzure(filepath, fileContentBytes.length, byteArrayInputStream);
    }
  }

  private void uploadToAzure(String filepath, long size, InputStream inputStream) {
    BlobClient blobClient = containerClient.getBlobClient(filepath);
    blobClient.upload(inputStream, size);
    log.info("finished uploading");
  }

  public void delete(String filepath) {
    log.info("Deleting file from Azure at filepath {}", filepath);
    BlobClient blobClient = containerClient.getBlobClient(filepath);
    blobClient.delete();
    log.info("Deleted file {}", filepath);
  }
}
