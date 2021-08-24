package org.codeforamerica.shiba.pages.data;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.Serial;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.codeforamerica.shiba.documents.CombinedDocumentRepositoryService;

@AllArgsConstructor
@Data
public class UploadedDocument implements Serializable {

    @Serial
    private static final long serialVersionUID = 6488007316203523563L;

    private String filename;
    private String s3Filepath;
    private String thumbnailFilepath;
    private String type;
    private long size;

    public String getThumbnail(CombinedDocumentRepositoryService documentRepositoryService) {
        try {
            var thumbnailBytes = documentRepositoryService
                    .getFromAzureWithFallbackToS3(thumbnailFilepath);
            return new String(thumbnailBytes, UTF_8);
        } catch (Exception e) {
            return "";
        }
    }
}
