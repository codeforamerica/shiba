package org.codeforamerica.shiba.documents;

import com.amazonaws.services.s3.AmazonS3Client;
import org.codeforamerica.shiba.pages.data.UploadedDocument;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("demo")
@Tag("externalServiceTest")
@SpringBootTest
class S3DocumentUploadServiceTest {
    @Autowired
    private S3DocumentUploadService s3DocumentUploadService;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    AmazonS3Client s3Client;

    @Value("classpath:shiba.jpg")
    private Resource shibaImg;

    @Test
    void itActuallyUploadsAndDeletes() throws IOException, InterruptedException {
        s3DocumentUploadService.upload("testshibaimg.jpg", (MultipartFile) shibaImg.getFile());
        assertThat(s3Client.listObjects().getObjectSummaries().size()).isEqualTo(1);
        s3DocumentUploadService.delete("testshibaimg.jpg");
        assertThat(s3Client.listObjects().getObjectSummaries().size()).isEqualTo(0);
    }
}