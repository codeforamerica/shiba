package org.codeforamerica.shiba.output.applicationinputsmappers;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.ApplicationInputType;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.Recipient;
import org.springframework.stereotype.Component;

@Component
public class MonthlyFrequencyInputsMapper implements ApplicationInputsMapper {

  @Override
  public List<ApplicationInput> map(Application application, Document document, Recipient recipient,
      SubworkflowIterationScopeTracker scopeTracker) {
    return List.of("unearnedIncomeSources", "unearnedIncomeSourcesCcap", "medicalExpensesSources")
        .stream()
        .flatMap(pageName ->
            Optional.ofNullable(application.getApplicationData().getPageData(pageName))
                .map(pageData -> pageData
                    .entrySet().stream()
                    .filter(inputData -> !inputData.getValue().getValue().isEmpty())
                    .map(inputData ->
                        new ApplicationInput(
                            pageName,
                            inputData.getKey().replace("Amount", "Frequency"),
                            List.of("Monthly"),
                            ApplicationInputType.SINGLE_VALUE)
                    ))
                .orElse(Stream.of()))
        .collect(Collectors.toList());
  }
}
