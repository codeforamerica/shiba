package org.codeforamerica.shiba.documents;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import org.codeforamerica.shiba.pages.data.UploadedDocument;
import org.springframework.web.multipart.MultipartFile;

public interface DocumentRepository {

  byte[] get(String filepath);

  void upload(String filepath, MultipartFile file) throws IOException, InterruptedException;

  void upload(String filepath, String fileContent) throws IOException, InterruptedException;

  void delete(String filepath);

  default String getThumbnail(UploadedDocument uploadedDocument) {
    try {
      var thumbnailBytes = get(uploadedDocument.getThumbnailFilepath());
      return new String(thumbnailBytes, UTF_8);
    } catch (Exception e) {
      return "";
    }
  }
}
