package org.codeforamerica.shiba.documents;

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

    public void uploadConcurrently(String filepath, MultipartFile file) {
        new Thread(() -> s3DocumentRepositoryService.upload(filepath, file)).start();
        new Thread(() -> azureDocumentRepositoryService.upload(filepath, file)).start();
    }

    public void deleteConcurrently(String filepath) {
        new Thread(() -> s3DocumentRepositoryService.delete(filepath)).start();
        new Thread(() -> azureDocumentRepositoryService.delete(filepath)).start();
    }
}
