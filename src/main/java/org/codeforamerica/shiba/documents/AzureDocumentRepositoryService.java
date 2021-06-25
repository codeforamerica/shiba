package org.codeforamerica.shiba.documents;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
//@Profile({"demo", "staging", "production"})
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
    public Runnable upload(String filepath, MultipartFile file) throws IOException, InterruptedException {
        log.info("Uploading file {} to Azure at filepath {}", file.getOriginalFilename(), filepath);
        // Get a reference to a blob
        BlobClient blobClient = containerClient.getBlobClient(filepath);
        log.info("Uploading to Azure Blob storage as blob:" + blobClient.getBlobUrl());
        // Upload the blob
        blobClient.upload(file.getInputStream(), file.getSize());
        log.info("finished uploading");
        return null;
    }

    @Override
    public Runnable delete(String filepath) {
        BlobClient blobClient = containerClient.getBlobClient(filepath);
        blobClient.delete();
        return null;
    }
}
