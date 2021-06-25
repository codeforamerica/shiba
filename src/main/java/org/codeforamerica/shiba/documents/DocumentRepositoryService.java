package org.codeforamerica.shiba.documents;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface DocumentRepositoryService {

    byte[] get(String filepath);

    Runnable upload(String filepath, MultipartFile file) throws IOException, InterruptedException;

    Runnable delete(String filepath);
}
