package org.codeforamerica.shiba.documents;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.transfer.TransferManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Service
//@Profile({"demo", "staging", "production", "test"})
@Slf4j
public class S3DocumentUploadService implements DocumentUploadService {
    private final TransferManager transferManager;
    private final String bucketName;
    private final AmazonS3 s3Client;
    private final ResourceLoader resourceLoader;

    public S3DocumentUploadService(
            TransferManager transferManager,
            @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") AmazonS3 s3Client, ResourceLoader resourceLoader) {
        this.s3Client = s3Client;
        this.bucketName = System.getenv("S3_BUCKET");
        this.transferManager = transferManager;
        this.resourceLoader = resourceLoader;
    }

    @Override
    public byte[] get(String filepath) {
        String s3Url = "s3://" + filepath;
        Resource resource = resourceLoader.getResource(s3Url);

        try (InputStream inputStream = resource.getInputStream()) {
            byte[] fileBytes = new byte[inputStream.available()];
            inputStream.read(fileBytes);
            return fileBytes;
        } catch (IOException e) {
            return null; //todo optional?
        }
    }

    @Override
    public void upload(String filepath, MultipartFile file) throws IOException, InterruptedException {
        log.info("Uploading file {} to S3 at filepath {}", file.getOriginalFilename(), filepath);
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        transferManager
                .upload(bucketName, filepath, file.getInputStream(), metadata)
                .waitForCompletion();
        log.info("finished uploading");
    }

    @Override
    public void delete(String filepath) throws SdkClientException {
        log.info("Deleting file at filepath {} from S3", filepath);
        s3Client.deleteObject(new DeleteObjectRequest(bucketName, filepath));
    }
}
