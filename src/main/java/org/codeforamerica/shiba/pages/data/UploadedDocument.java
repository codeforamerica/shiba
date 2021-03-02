package org.codeforamerica.shiba.pages.data;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class UploadedDocument {
    private String filename;
    private long size;
}
