package org.codeforamerica.shiba.documents;

import org.springframework.web.multipart.MultipartFile;

public interface DocumentRepositoryService {

    byte[] get(String filepath);

    void upload(String filepath, MultipartFile file);

    Runnable delete(String filepath);
}
