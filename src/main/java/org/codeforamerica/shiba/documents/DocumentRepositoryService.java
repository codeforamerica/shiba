package org.codeforamerica.shiba.documents;

import org.springframework.web.multipart.MultipartFile;

public interface DocumentRepositoryService {

    byte[] get(String filepath);

    void upload(String filepath, MultipartFile file);

    void delete(String filepath);
}
