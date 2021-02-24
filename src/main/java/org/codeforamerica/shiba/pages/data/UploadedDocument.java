package org.codeforamerica.shiba.pages.data;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UploadedDocument {
    private String filename;
    private MultipartFile file;
}
