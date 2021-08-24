package org.codeforamerica.shiba.documents;

import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;

public interface DocumentRepositoryService {

  byte[] get(String filepath);

  void upload(String filepath, MultipartFile file) throws IOException;

  void delete(String filepath);
}
