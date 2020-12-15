package org.codeforamerica.shiba.output.caf;

import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.parsers.GrossMonthlyIncomeParser;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.output.ApplicationInputType.SINGLE_VALUE;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GrossMonthlyIncomeMapperTest {
    private final ApplicationData applicationData = new ApplicationData();
    private final GrossMonthlyIncomeParser grossMonthlyIncomeParser = mock(GrossMonthlyIncomeParser.class);

    @Test
    void shouldMapJobIncomeInformationToInputs() {
        GrossMonthlyIncomeMapper grossMonthlyIncomeMapper = new GrossMonthlyIncomeMapper(grossMonthlyIncomeParser);
        Application application = Application.builder().applicationData(applicationData).build();
        when(grossMonthlyIncomeParser.parse(applicationData)).thenReturn(List.of(
                new HourlyJobIncomeInformation("12", "30", 0),
                new HourlyJobIncomeInformation("6", "45", 1)
        ));
        List<ApplicationInput> applicationInputs = grossMonthlyIncomeMapper.map(application, Recipient.CLIENT, null);

        assertThat(applicationInputs).contains(
                new ApplicationInput("employee", "grossMonthlyIncome", List.of("1440.0"), SINGLE_VALUE, 0),
                new ApplicationInput("employee", "grossMonthlyIncome", List.of("1080.0"), SINGLE_VALUE, 1)
        );
    }
}