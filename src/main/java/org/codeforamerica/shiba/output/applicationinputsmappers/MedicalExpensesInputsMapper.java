package org.codeforamerica.shiba.output.applicationinputsmappers;

import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.MEDICAL_EXPENSES;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getValues;

import java.util.Collections;
import java.util.List;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.ApplicationInputType;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.springframework.stereotype.Component;

@Component
public class MedicalExpensesInputsMapper implements ApplicationInputsMapper {

  @Override
  public List<ApplicationInput> map(Application application, Document _document,
      Recipient _recipient, SubworkflowIterationScopeTracker _scopeTracker) {
    return map(application.getApplicationData().getPagesData());
  }

  private List<ApplicationInput> map(PagesData pagesData) {
    List<String> medicalExpenses = getValues(pagesData, MEDICAL_EXPENSES);
    if (medicalExpenses.isEmpty()) {
      return Collections.emptyList();
    }

    return List.of(new ApplicationInput("medicalExpenses", "medicalExpensesSelection",
        List.of(medicalExpenses.contains("NONE_OF_THE_ABOVE") ? "NONE_SELECTED" : "ONE_SELECTED"),
        ApplicationInputType.SINGLE_VALUE));
  }
}
