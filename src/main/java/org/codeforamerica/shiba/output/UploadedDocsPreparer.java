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
   * Returns a combined list of uploaded docs that have been renamed to meet the standards of MNIT, converted
   * to PDFs (when possible), and have had cover pages added (when possible) Note: the filenames for
   * these documents will include the county dhsProviderId. Those filenames are changed later to
   * include the dhsProviderId specific to whatever RoutingDestination the file is being sent to
   */
  public List<ApplicationFile> prepare(List<UploadedDocument> uploadedDocs,
      Application application) {
    
    List<ApplicationFile> applicationFiles = new ArrayList<>();
    
    byte[] coverPage = pdfGenerator.generateCoverPageForUploadedDocs(application);
    
    List<ApplicationFile> preparedDocument = pdfGenerator.generateCombinedUploadedDocument(uploadedDocs, application, coverPage);
    
    if (preparedDocument != null && preparedDocument.stream().allMatch(uDoc -> uDoc.getFileBytes().length > 0)) {
      applicationFiles = preparedDocument;
    }
    return applicationFiles;
  }
}

