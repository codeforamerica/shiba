package org.codeforamerica.shiba.output.documentfieldpreparers;

import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.output.DocumentFieldType;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.HAVE_HEALTHCARE_COVERAGE;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.REGISTER_TO_VOTE;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getFirstValue;

@Component
public class HealthcareCoveragePreparer implements DocumentFieldPreparer {

  @Override
  public List<DocumentField> prepareDocumentFields(Application application, Document _document,
      Recipient _recipient) {
    return map(application.getApplicationData().getPagesData());
  }

  private List<DocumentField> map(PagesData pagesData) {
    String healthcareCoverage = getFirstValue(pagesData, HAVE_HEALTHCARE_COVERAGE);
    if (healthcareCoverage == null) {
      // Not answered
      return Collections.emptyList();
    }

    return switch (healthcareCoverage) {
      case "NO" -> createApplicationInput("false");
      case "YES" -> createApplicationInput("true");
      case "NOT_SURE" -> Collections.emptyList();
      default -> Collections.emptyList();
    };
  }

  @NotNull
  private List<DocumentField> createApplicationInput(String value) {
    return List.of(new DocumentField("healthcareCoverage", "healthcareCoverage",
        List.of(value),
        DocumentFieldType.ENUMERATED_SINGLE_VALUE));
  }
}
