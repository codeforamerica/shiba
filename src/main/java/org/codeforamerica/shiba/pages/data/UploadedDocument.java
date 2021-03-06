package org.codeforamerica.shiba.pages.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

@AllArgsConstructor
@Data
@ToString(exclude = {"dataURL"})
public class UploadedDocument implements Serializable {
    @Serial
    private static final long serialVersionUID = 6488007316203523563L;

    private String filename;
    private String s3Filepath;
    private String dataURL; // thumbnail image as a string, generated by dropzone
    private String type;
    private long size;
}
