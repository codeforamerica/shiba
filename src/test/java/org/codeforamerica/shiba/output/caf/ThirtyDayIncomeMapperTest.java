package org.codeforamerica.shiba.output.caf;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import org.codeforamerica.shiba.Money;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.parsers.TotalIncomeParser;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.output.DocumentFieldType;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.junit.jupiter.api.Test;

class ThirtyDayIncomeMapperTest {

  private final TotalIncomeCalculator totalIncomeCalculator = mock(TotalIncomeCalculator.class);
  private final TotalIncomeParser totalIncomeParser = mock(TotalIncomeParser.class);
  ThirtyDayIncomeMapper thirtyDayIncomeMapper = new ThirtyDayIncomeMapper(totalIncomeCalculator,
      totalIncomeParser);

  @Test
  void returnsCalculatedTotalIncome() {
    ApplicationData appData = new ApplicationData();
    Application application = Application.builder().applicationData(appData).build();

    List<JobIncomeInformation> jobIncomeInformationList = List.of();
    Money thirtyDayIncome = Money.ONE;
    when(totalIncomeParser.parse(appData))
        .thenReturn(new TotalIncome(thirtyDayIncome, jobIncomeInformationList));
    when(
        totalIncomeCalculator.calculate(new TotalIncome(thirtyDayIncome, jobIncomeInformationList)))
        .thenReturn(Money.parse("111"));

    assertThat(
        thirtyDayIncomeMapper.prepareDocumentFields(application, null, Recipient.CLIENT, null))
        .isEqualTo(List.of(
            new DocumentField(
                "totalIncome",
                "thirtyDayIncome",
                List.of("111.00"),
                DocumentFieldType.SINGLE_VALUE
            )
        ));
  }
}
