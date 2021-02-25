package org.codeforamerica.shiba.pages.data;

import lombok.Data;

@Data
public class UploadedDocument {
    private String filename;
    private long size;
}
