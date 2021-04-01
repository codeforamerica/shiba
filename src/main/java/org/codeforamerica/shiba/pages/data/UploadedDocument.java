package org.codeforamerica.shiba.pages.data;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class UploadedDocument {
    private String filename;
    private String s3Filepath;
    private String dataURL;
    private String type;
    private long size;
}
