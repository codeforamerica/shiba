package org.codeforamerica.shiba.output;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import org.codeforamerica.shiba.Money;
import org.codeforamerica.shiba.application.parsers.GrossMonthlyIncomeParser;
import org.codeforamerica.shiba.application.parsers.TotalIncomeParser;
import org.codeforamerica.shiba.output.caf.JobIncomeInformation;
import org.codeforamerica.shiba.output.caf.TotalIncome;
import org.codeforamerica.shiba.output.caf.UnearnedIncomeCalculator;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.testutilities.TestApplicationDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TotalIncomeParserTest {

  private TotalIncomeParser totalIncomeParser;
  private GrossMonthlyIncomeParser grossIncomeParser;
  private UnearnedIncomeCalculator unearnedIncomeCalculator;

  @BeforeEach
  void setUp() {
    grossIncomeParser = mock(GrossMonthlyIncomeParser.class);
    unearnedIncomeCalculator = mock(UnearnedIncomeCalculator.class);
    totalIncomeParser = new TotalIncomeParser(grossIncomeParser);
  }

  @Test
  void shouldParseTotalIncomeFromLastThirtyDaysAndJobIncomeInformation() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("thirtyDayIncome", "moneyMadeLast30Days", "1")
        .withPageData("unearnedIncome", "unearnedIncome", List.of("NO_UNEARNED_INCOME_SELECTED"))
        .build();

    JobIncomeInformation mockJobInfo1 = mock(JobIncomeInformation.class);
    JobIncomeInformation mockJobInfo2 = mock(JobIncomeInformation.class);
    List<JobIncomeInformation> jobInfo = List.of(mockJobInfo1, mockJobInfo2);
    when(grossIncomeParser.parse(eq(applicationData))).thenReturn(jobInfo);

    TotalIncome totalIncome = totalIncomeParser.parse(applicationData);

    assertThat(totalIncome).isEqualTo(new TotalIncome(Money.ONE, jobInfo));
  }
}
