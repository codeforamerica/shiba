package org.codeforamerica.shiba.output.caf;

import java.util.List;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.parsers.TotalIncomeParser;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.output.DocumentFieldType;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.output.documentfieldpreparers.DocumentFieldPreparer;
import org.springframework.stereotype.Component;

@Component
public class ThirtyDayIncomePreparer implements DocumentFieldPreparer {

  private final TotalIncomeCalculator totalIncomeCalculator;
  private final TotalIncomeParser totalIncomeParser;

  public ThirtyDayIncomePreparer(
      TotalIncomeCalculator totalIncomeCalculator,
      TotalIncomeParser totalIncomeParser
  ) {
    this.totalIncomeCalculator = totalIncomeCalculator;
    this.totalIncomeParser = totalIncomeParser;
  }

  @Override
  public List<DocumentField> prepareDocumentFields(Application application, Document document,
      Recipient recipient) {
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
