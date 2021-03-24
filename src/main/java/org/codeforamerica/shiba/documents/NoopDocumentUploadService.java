package org.codeforamerica.shiba.documents;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Profile("!demo & !staging & !production")
@Slf4j
public class NoopDocumentUploadService implements DocumentUploadService {
    @Override
    public void upload(String filepath, MultipartFile file) {
        log.info("Pretending to upload file {} to s3 with filepath {}", file.getOriginalFilename(), filepath);
    }

    @Override
    public void delete(String filepath) {
        log.info("Pretending to delete file from s3: {}", filepath);
    }
}

