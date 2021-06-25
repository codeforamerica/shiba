package org.codeforamerica.shiba.documents;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class CombinedAzureS3DocumentRepositoryService {
    private final S3DocumentRepositoryService s3DocumentRepositoryService;
    private final AzureDocumentRepositoryService azureDocumentRepositoryService;

    public CombinedAzureS3DocumentRepositoryService(S3DocumentRepositoryService s3DocumentRepositoryService,
                                                    AzureDocumentRepositoryService azureDocumentRepositoryService) {
        this.s3DocumentRepositoryService = s3DocumentRepositoryService;
        this.azureDocumentRepositoryService = azureDocumentRepositoryService;
    }

    public byte[] getFromAzureWithFallbackToS3(String filepath) {
        byte[] content = azureDocumentRepositoryService.get(filepath);
        if (null == content) {
            content = s3DocumentRepositoryService.get(filepath);
        }

        return content;
    }

    public void uploadConcurrently(String filepath, MultipartFile file) throws IOException, InterruptedException {
        new Thread(s3DocumentRepositoryService.upload(filepath, file)).start();
        new Thread(azureDocumentRepositoryService.upload(filepath, file)).start();
    }

    public void deleteConcurrently(String filepath) {
        new Thread(s3DocumentRepositoryService.delete(filepath)).start();
        new Thread(azureDocumentRepositoryService.delete(filepath)).start();
    }
}
