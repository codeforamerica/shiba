package org.codeforamerica.shiba.pages.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

@AllArgsConstructor
@Data
@ToString(exclude = {"dataURL"})
public class UploadedDocument implements Serializable {
    private String filename;
    private String s3Filepath;
    private String dataURL;
    private String type;
    private long size;
}
