package org.codeforamerica.shiba.documents;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Service
@Slf4j
public class AzureDocumentRepositoryService implements DocumentRepositoryService {
    private final BlobContainerClient containerClient;

    public AzureDocumentRepositoryService() {
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().connectionString(System.getenv("AZURE_CONNECTION_STRING")).buildClient();
        this.containerClient = blobServiceClient.getBlobContainerClient(System.getenv("AZURE_CONTAINER_NAME"));
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
        // Get a reference to a blob
        BlobClient blobClient = containerClient.getBlobClient(filepath);
        log.info("Uploading to Azure Blob storage as blob:" + blobClient.getBlobUrl());
        try (var inputStream = file.getInputStream()) {
            blobClient.upload(inputStream, file.getSize());
            log.info("finished uploading");
        }
    }

    public void upload(String filepath, String fileContent) throws IOException {
        log.info("Uploading file content string to Azure at filepath {}", filepath);

        // Get a reference to a blob
        BlobClient blobClient = containerClient.getBlobClient(filepath);
        log.info("Uploading to Azure Blob storage as blob:" + blobClient.getBlobUrl());

        // Upload the blob
        var fileContentBytes = fileContent.getBytes(StandardCharsets.UTF_8);
        try (var byteArrayInputStream = new ByteArrayInputStream(fileContentBytes)) {
            blobClient.upload(byteArrayInputStream, fileContentBytes.length);
            log.info("finished uploading");
        }
    }

    @Override
    public Runnable delete(String filepath) {
        BlobClient blobClient = containerClient.getBlobClient(filepath);
        blobClient.delete();
        return null;
    }
}
