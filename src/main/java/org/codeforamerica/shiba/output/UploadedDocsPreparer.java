package org.codeforamerica.shiba.output;

import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.pdf.PdfGenerator;
import org.codeforamerica.shiba.pages.data.UploadedDocument;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UploadedDocsPreparer {

  private final PdfGenerator pdfGenerator;

  public UploadedDocsPreparer(PdfGenerator pdfGenerator) {
    this.pdfGenerator = pdfGenerator;
  }

  /**
   * Returns a list of uploaded docs that have been renamed to meet the standards of MNIT, converted
   * to PDFs (when possible), and have had cover pages added (when possible) Note: the filenames for
   * these documents will include the county dhsProviderId. Those filenames are changed later to
   * include the dhsProviderId specific to whatever RoutingDestination the file is being sent to
   */
  public List<ApplicationFile> prepare(List<UploadedDocument> uploadedDocs,
      Application application) {
    List<ApplicationFile> applicationFiles = new ArrayList<>();
    byte[] coverPage = pdfGenerator.generateCoverPageForUploadedDocs(application);
    for (int i = 0; i < uploadedDocs.size(); i++) {
      UploadedDocument originalDocument = uploadedDocs.get(i);
      try {
        ApplicationFile preparedDocument = pdfGenerator.generateForUploadedDocument(originalDocument, i, application, coverPage);
        if (preparedDocument != null && preparedDocument.getFileBytes().length > 0) {
          applicationFiles.add(preparedDocument);
        }
      } catch (NullPointerException e) {
        log.warn("Null Pointer Exception Caught while preparing document "
            + originalDocument.getSysFileName() + " to send " + e.getMessage());
        ApplicationFile preparedDocument =
            pdfGenerator.generateForUploadedDocument(originalDocument, i, application, null);
        log.info("Now queueing file to send: %s".formatted(preparedDocument.getFileName())
            + " without cover page");
        applicationFiles.add(preparedDocument);
      }
    }
    return applicationFiles;
  }
}

