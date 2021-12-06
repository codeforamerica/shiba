package org.codeforamerica.shiba.application.parsers;

import static org.codeforamerica.shiba.output.Document.CAF;
import static org.codeforamerica.shiba.output.Document.CCAP;
import static org.codeforamerica.shiba.output.Document.CERTAIN_POPS;

import java.util.ArrayList;
import java.util.List;
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

    return documents;
  }
}
