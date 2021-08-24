package org.codeforamerica.shiba.output;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import org.codeforamerica.shiba.Money;
import org.codeforamerica.shiba.application.parsers.GrossMonthlyIncomeParser;
import org.codeforamerica.shiba.application.parsers.TotalIncomeParser;
import org.codeforamerica.shiba.output.caf.JobIncomeInformation;
import org.codeforamerica.shiba.output.caf.TotalIncome;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.InputData;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.codeforamerica.shiba.pages.data.Subworkflows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TotalIncomeParserTest {

  private final ApplicationData applicationData = new ApplicationData();
  private final PagesData pagesData = new PagesData();
  private final Subworkflows subworkflows = new Subworkflows();

  private TotalIncomeParser totalIncomeParser;
  private GrossMonthlyIncomeParser grossIncomeParser;

  @BeforeEach
  void setUp() {
    applicationData.setPagesData(pagesData);
    applicationData.setSubworkflows(subworkflows);

    grossIncomeParser = mock(GrossMonthlyIncomeParser.class);
    totalIncomeParser = new TotalIncomeParser(grossIncomeParser);
  }

  @Test
  void shouldParseTotalIncomeFromLastThirtyDaysAndJobIncomeInformation() {
    pagesData.putPage("thirtyDayIncome", new PageData(
        Map.of("moneyMadeLast30Days", InputData.builder().value(List.of("1")).build())));
    JobIncomeInformation mockJobInfo1 = mock(JobIncomeInformation.class);
    JobIncomeInformation mockJobInfo2 = mock(JobIncomeInformation.class);
    List<JobIncomeInformation> jobInfo = List.of(mockJobInfo1, mockJobInfo2);
    when(grossIncomeParser.parse(eq(applicationData))).thenReturn(jobInfo);

    TotalIncome totalIncome = totalIncomeParser.parse(applicationData);

    assertThat(totalIncome).isEqualTo(new TotalIncome(Money.ONE, jobInfo));
  }
}
