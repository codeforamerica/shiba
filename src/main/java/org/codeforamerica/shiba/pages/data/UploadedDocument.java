package org.codeforamerica.shiba.pages.data;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@AllArgsConstructor
@Data
public class UploadedDocument implements Serializable {
    private String filename;
    private String s3Filepath;
    private long size;
}
