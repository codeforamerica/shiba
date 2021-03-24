package org.codeforamerica.shiba.documents;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;

@ActiveProfiles("demo")
@Tag("externalServiceTest")
@SpringBootTest
class S3DocumentUploadServiceTest {
    @Autowired
    private S3DocumentUploadService s3DocumentUploadService;

    @Value("classpath:shiba.jpg")
    private Resource shibaImg;

    @Test
    void itActuallyUploads() throws IOException {
        s3DocumentUploadService.upload("testshibaimg.jpg", shibaImg.getFile());
    }
}