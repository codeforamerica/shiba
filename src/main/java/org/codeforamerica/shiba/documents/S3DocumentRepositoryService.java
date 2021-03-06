package org.codeforamerica.shiba.documents;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.util.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@Slf4j
public class S3DocumentRepositoryService implements DocumentRepositoryService {
    private final TransferManager transferManager;
    private final String bucketName;
    private final AmazonS3 s3Client;

    public S3DocumentRepositoryService(
            TransferManager transferManager,
            @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") AmazonS3 s3Client, ResourceLoader resourceLoader) {
        this.s3Client = s3Client;
        this.bucketName = System.getenv("S3_BUCKET");
        this.transferManager = transferManager;
    }

    @Override
    public byte[] get(String filepath) {
        try {
            S3Object obj = s3Client.getObject(bucketName, filepath);
            S3ObjectInputStream stream = obj.getObjectContent();
            byte[] content = IOUtils.toByteArray(stream);
            obj.close();
            return content;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Runnable upload(String filepath, MultipartFile file) {
        log.info("Uploading file {} to S3 at filepath {}", file.getOriginalFilename(), filepath);
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        try {
            transferManager
                    .upload(bucketName, filepath, file.getInputStream(), metadata)
                    .waitForCompletion();
            log.info("finished uploading");
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public Runnable delete(String filepath) throws SdkClientException {
        log.info("Deleting file at filepath {} from S3", filepath);
        s3Client.deleteObject(new DeleteObjectRequest(bucketName, filepath));
        return null;
    }
}
