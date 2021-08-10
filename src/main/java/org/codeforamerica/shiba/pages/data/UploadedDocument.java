package org.codeforamerica.shiba.pages.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;
import org.codeforamerica.shiba.documents.CombinedDocumentRepositoryService;
import org.codeforamerica.shiba.documents.DocumentRepositoryService;
import org.codeforamerica.shiba.output.Document;

import java.io.Serial;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;

import static java.nio.charset.StandardCharsets.UTF_8;

@AllArgsConstructor
@Data
public class UploadedDocument implements Serializable {
    @Serial
    private static final long serialVersionUID = 6488007316203523563L;

    private String filename;
    private String s3Filepath;

    public String getThumbnail(CombinedDocumentRepositoryService documentRepositoryService) {
        try {
            var thumbnailBytes = documentRepositoryService.getFromAzureWithFallbackToS3(thumbnailFilepath);
            return new String(thumbnailBytes, UTF_8);
        } catch (Exception e) {
            return "";
        }
    }

    private String thumbnailFilepath;
    private String type;
    private long size;
}
