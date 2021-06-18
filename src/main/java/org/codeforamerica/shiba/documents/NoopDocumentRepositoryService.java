//package org.codeforamerica.shiba.documents;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.context.annotation.Profile;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.IOException;
//
//@Service
//@Profile("!demo & !staging & !production")
//@Slf4j
//public class NoopDocumentRepositoryService implements DocumentRepositoryService {
//    @Override
//    public byte[] get(String filepath) {
//        return new byte[]{};
//    }
//
//    @Override
//    public void upload(String filepath, MultipartFile file) throws IOException, InterruptedException {
//        log.info("Pretending to upload file {} to s3 with filepath {}", file.getOriginalFilename(), filepath);
//    }
//
//    @Override
//    public void delete(String filepath) {
//        log.info("Pretending to delete file from s3: {}", filepath);
//    }
//}
