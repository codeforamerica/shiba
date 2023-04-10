package org.codeforamerica.shiba.application.parsers;

import static org.codeforamerica.shiba.output.Document.CAF;
import static org.codeforamerica.shiba.output.Document.CCAP;
import static org.codeforamerica.shiba.output.Document.CERTAIN_POPS;
import static org.codeforamerica.shiba.output.Document.UPLOADED_DOC;

import java.util.ArrayList;
import java.util.List;

import org.codeforamerica.shiba.application.FlowType;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.pages.data.ApplicationData;

public class DocumentListParser {

  public static List<Document> parse(ApplicationData applicationData) {
    ArrayList<Document> documents = new ArrayList<>();
    if (applicationData.isCCAPApplication()) {
      documents.add(CCAP);
    }
    if (applicationData.isCAFApplication()) {
      documents.add(CAF);
    }
    if (applicationData.isCertainPopsApplication()) {
      documents.add(CERTAIN_POPS);
    }
    
    if (applicationData.getFlow() != null && applicationData.getFlow().equals(FlowType.LATER_DOCS)) {
        documents.add(UPLOADED_DOC);
      }
    if (applicationData.getFlow() != null && applicationData.getFlow().equals(FlowType.HEALTHCARE_RENEWAL)) {
        documents.add(UPLOADED_DOC);
      }

    return documents;
  }
}
