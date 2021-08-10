package org.codeforamerica.shiba.documents;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface DocumentRepositoryService {

    byte[] get(String filepath);

    void upload(String filepath, MultipartFile file) throws IOException;

    void delete(String filepath);
}
