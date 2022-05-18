package org.codeforamerica.shiba.output.documentfieldpreparers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.output.DocumentFieldType;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.pages.data.InputData;
import org.codeforamerica.shiba.pages.data.PageData;
import org.springframework.stereotype.Component;

@Component
public class MonthlyFrequencyPreparer implements DocumentFieldPreparer {

  @Override
  public List<DocumentField> prepareDocumentFields(Application application, Document document,
      Recipient recipient) {

    List<DocumentField> fields = new ArrayList<>();
    List<String> pageNames = List.of("unearnedIncomeSources", "unearnedIncomeSourcesCcap",
        "medicalExpensesSources", "socialSecurityIncomeSource", "supplementalSecurityIncomeSource", 
        "veteransBenefitsIncomeSource", "unemploymentIncomeSource", "workersCompIncomeSource",
        "retirementIncomeSource", "childOrSpousalSupportIncomeSource", "tribalPaymentIncomeSource");

    for (String pageName : pageNames) {
      Optional<PageData> optionalPageData = Optional.ofNullable(
          application.getApplicationData().getPageData(pageName)
      );

      optionalPageData.ifPresent(pageData -> {
        Set<Entry<String, InputData>> entries = pageData.entrySet();

        List<DocumentField> fieldsForThisPage = entries.stream()
            .filter(inputData -> !inputData.getValue().getValue().isEmpty())
            .map(inputData -> new DocumentField(
                pageName,
                inputData.getKey().replace("Amount", "Frequency"),
                List.of("Monthly"),
                DocumentFieldType.SINGLE_VALUE))
            .toList();
        fields.addAll(fieldsForThisPage);
      });
    }

    return fields;
  }
}
