package org.codeforamerica.shiba.output.documentfieldpreparers;

import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.MEDICAL_EXPENSES;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getValues;

import java.util.Collections;
import java.util.List;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.output.DocumentFieldType;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.springframework.stereotype.Component;

@Component
public class MedicalExpensesPreparer implements DocumentFieldPreparer {

  @Override
  public List<DocumentField> prepareDocumentFields(Application application, Document _document,
      Recipient _recipient, SubworkflowIterationScopeTracker _scopeTracker) {
    return map(application.getApplicationData().getPagesData());
  }

  private List<DocumentField> map(PagesData pagesData) {
    List<String> medicalExpenses = getValues(pagesData, MEDICAL_EXPENSES);
    if (medicalExpenses.isEmpty()) {
      return Collections.emptyList();
    }

    return List.of(new DocumentField("medicalExpenses", "medicalExpensesSelection",
        List.of(medicalExpenses.contains("NONE_OF_THE_ABOVE") ? "NONE_SELECTED" : "ONE_SELECTED"),
        DocumentFieldType.SINGLE_VALUE));
  }
}
