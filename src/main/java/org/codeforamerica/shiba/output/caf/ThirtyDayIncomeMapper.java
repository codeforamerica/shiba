package org.codeforamerica.shiba.output.caf;

import java.util.List;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.parsers.TotalIncomeParser;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.output.DocumentFieldType;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.output.applicationinputsmappers.ApplicationInputsMapper;
import org.codeforamerica.shiba.output.applicationinputsmappers.SubworkflowIterationScopeTracker;
import org.springframework.stereotype.Component;

@Component
public class ThirtyDayIncomeMapper implements ApplicationInputsMapper {

  private final TotalIncomeCalculator totalIncomeCalculator;
  private final TotalIncomeParser totalIncomeParser;

  public ThirtyDayIncomeMapper(
      TotalIncomeCalculator totalIncomeCalculator,
      TotalIncomeParser totalIncomeParser
  ) {
    this.totalIncomeCalculator = totalIncomeCalculator;
    this.totalIncomeParser = totalIncomeParser;
  }

  @Override
  public List<DocumentField> prepareDocumentFields(Application application, Document document,
      Recipient recipient,
      SubworkflowIterationScopeTracker scopeTracker) {
    TotalIncome totalIncome = totalIncomeParser.parse(application.getApplicationData());
    return List.of(
        new DocumentField(
            "totalIncome",
            "thirtyDayIncome",
            List.of(totalIncomeCalculator.calculate(totalIncome).toString()),
            DocumentFieldType.SINGLE_VALUE
        )
    );
  }
}
