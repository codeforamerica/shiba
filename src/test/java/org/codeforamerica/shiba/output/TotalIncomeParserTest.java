package org.codeforamerica.shiba.output;

import org.codeforamerica.shiba.application.parsers.AbstractParserTest;
import org.codeforamerica.shiba.application.parsers.GrossMonthlyIncomeParser;
import org.codeforamerica.shiba.application.parsers.TotalIncomeParser;
import org.codeforamerica.shiba.output.caf.JobIncomeInformation;
import org.codeforamerica.shiba.output.caf.TotalIncome;
import org.codeforamerica.shiba.pages.data.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TotalIncomeParserTest extends AbstractParserTest {
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
        totalIncomeParser = new TotalIncomeParser(parsingConfiguration, grossIncomeParser);
    }

    @Test
    void shouldParseTotalIncomeFromLastThirtyDaysAndJobIncomeInformation() {
        pagesData.putPage("incomePage", new PageData(Map.of("incomeInput", InputData.builder().value(List.of("1.0")).build())));
        JobIncomeInformation mockJobInfo1 = mock(JobIncomeInformation.class);
        JobIncomeInformation mockJobInfo2 = mock(JobIncomeInformation.class);
        List<JobIncomeInformation> jobInfo = List.of(mockJobInfo1, mockJobInfo2);
        when(grossIncomeParser.parse(eq(applicationData))).thenReturn(jobInfo);

        TotalIncome totalIncome = totalIncomeParser.parse(applicationData);

        assertThat(totalIncome).isEqualTo(new TotalIncome(1.0, jobInfo));
    }
}