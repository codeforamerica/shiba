package org.codeforamerica.shiba.output.caf;

import java.util.List;

import org.codeforamerica.shiba.Money;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.parsers.TotalIncomeParser;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.output.DocumentFieldType;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.output.documentfieldpreparers.DocumentFieldPreparer;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.springframework.stereotype.Component;

@Component
public class ThirtyDayIncomePreparer implements DocumentFieldPreparer {

  private final TotalIncomeCalculator totalIncomeCalculator;
  private final TotalIncomeParser totalIncomeParser;
  private final UnearnedIncomeCalculator unearnedIncomeCalculator;

  public ThirtyDayIncomePreparer(
	  TotalIncomeCalculator totalIncomeCalculator,
	  TotalIncomeParser totalIncomeParser,
      UnearnedIncomeCalculator unearnedIncomeCalculator
  ) {
    this.totalIncomeCalculator = totalIncomeCalculator;
    this.totalIncomeParser = totalIncomeParser;
    this.unearnedIncomeCalculator = unearnedIncomeCalculator;
  }

@Override
  public List<DocumentField> prepareDocumentFields(Application application, Document document,
      Recipient recipient) {
	ApplicationData applicationData = application.getApplicationData();
    TotalIncome totalIncomeEarned = totalIncomeParser.parse(applicationData);
    Money earnedIncome = totalIncomeCalculator.calculate(totalIncomeEarned);
    Money unearnedIncome = unearnedIncomeCalculator.unearnedAmount(applicationData);
    if (unearnedIncome == null) unearnedIncome = Money.ZERO;
    Money earnedAndUnearnedIncome = earnedIncome.add(unearnedIncome);
    return List.of(
        new DocumentField(
            "totalIncome",
            "thirtyDayIncome",
            List.of(earnedAndUnearnedIncome.toString()),
            DocumentFieldType.SINGLE_VALUE
        )
    );
  }
}
