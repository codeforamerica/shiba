package org.codeforamerica.shiba.pages.data;

import java.io.Serial;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UploadedDocument implements Serializable {

  @Serial
  private static final long serialVersionUID = 6488007316203523563L;

  private String filename;
  private String s3Filepath; // Changing this name would break in-progress applications during the next deploy
  private String thumbnailFilepath;
  private String type;
  private long size; // bytes
  private String sysFileName;
  
  public UploadedDocument(String filename, String s3Filepath, String thumbnailFilepath, String type,
      long size) {
    super();
    this.filename = filename;
    this.s3Filepath = s3Filepath;
    this.thumbnailFilepath = thumbnailFilepath;
    this.type = type;
    this.size = size;
  }
}
