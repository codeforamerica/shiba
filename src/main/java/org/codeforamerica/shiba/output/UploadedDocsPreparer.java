package org.codeforamerica.shiba.output;

import java.util.ArrayList;
import java.util.List;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.pdf.PdfGenerator;
import org.codeforamerica.shiba.pages.data.UploadedDocument;
import org.springframework.stereotype.Component;

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
  public List<ApplicationFile> prepare(List<UploadedDocument> uploadedDocs, Application application) {
    List<ApplicationFile> applicationFiles = new ArrayList<>();
    byte[] coverPage = pdfGenerator.generateCoverPageForUploadedDocs(application);
    for (int i = 0; i < uploadedDocs.size(); i++) {
      UploadedDocument originalDocument = uploadedDocs.get(i);
      ApplicationFile preparedDocument =
          pdfGenerator.generateForUploadedDocument(originalDocument, i, application, coverPage);
      if (preparedDocument != null && preparedDocument.getFileBytes().length > 0) {
        applicationFiles.add(preparedDocument);
      }
    }

    return applicationFiles;
  }
}
