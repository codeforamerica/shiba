package org.codeforamerica.shiba;


import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.transfer.TransferManager;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class S3DocumentUploadService implements DocumentUploadService {
    private final TransferManager transferManager;
    private final String bucketName;
    private final AmazonS3 s3Client;

    public S3DocumentUploadService(TransferManager transferManager, @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") AmazonS3 s3Client) {
        this.s3Client = s3Client;
        bucketName = System.getenv("S3-BUCKET");
        this.transferManager = transferManager;
    }

    @Override
    public void upload(String filepath, MultipartFile file) throws IOException, InterruptedException {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        transferManager
                .upload(bucketName, filepath, file.getInputStream(), metadata)
                .waitForCompletion();
    }

    @Override
    public void delete(String filepath) {
        try {
            s3Client.deleteObject(new DeleteObjectRequest(bucketName, filepath));
        } catch (SdkClientException e) {
            e.printStackTrace();
        }
    }
}
