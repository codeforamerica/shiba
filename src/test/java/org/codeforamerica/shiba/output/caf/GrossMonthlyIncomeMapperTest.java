package org.codeforamerica.shiba.output.caf;

import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.parsers.GrossMonthlyIncomeParser;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.applicationinputsmappers.SubworkflowIterationScopeTracker;
import org.codeforamerica.shiba.pages.config.ApplicationConfiguration;
import org.codeforamerica.shiba.pages.config.PageGroupConfiguration;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.output.ApplicationInputType.SINGLE_VALUE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GrossMonthlyIncomeMapperTest {
    private final ApplicationData applicationData = new ApplicationData();
    private final GrossMonthlyIncomeParser grossMonthlyIncomeParser = mock(GrossMonthlyIncomeParser.class);
    private final ApplicationConfiguration applicationConfiguration = mock(ApplicationConfiguration.class);
    private final SubworkflowIterationScopeTracker scopeTracker = mock(SubworkflowIterationScopeTracker.class);

    @Test
    void shouldMapJobIncomeInformationToInputs() {
        when(scopeTracker.getIterationScopeInfo(any(), any())).thenReturn(
                new SubworkflowIterationScopeTracker.IterationScopeInfo("prefix", 0),
                new SubworkflowIterationScopeTracker.IterationScopeInfo("prefix", 1));
        when(applicationConfiguration.getPageGroups()).thenReturn(Map.of("jobs", mock(PageGroupConfiguration.class)));
        GrossMonthlyIncomeMapper grossMonthlyIncomeMapper = new GrossMonthlyIncomeMapper(grossMonthlyIncomeParser, applicationConfiguration);
        Application application = Application.builder().applicationData(applicationData).build();
        when(grossMonthlyIncomeParser.parse(applicationData)).thenReturn(List.of(
                new HourlyJobIncomeInformation("12", "30", 0, null),
                new HourlyJobIncomeInformation("6", "45", 1, null)
        ));
        List<ApplicationInput> applicationInputs = grossMonthlyIncomeMapper.map(application, null, scopeTracker);

        assertThat(applicationInputs).contains(
                new ApplicationInput("employee", "grossMonthlyIncome", List.of("1440.0"), SINGLE_VALUE, 0),
                new ApplicationInput("prefix_employee", "grossMonthlyIncome", List.of("1440.0"), SINGLE_VALUE, 0),
                new ApplicationInput("employee", "grossMonthlyIncome", List.of("1080.0"), SINGLE_VALUE, 1),
                new ApplicationInput("prefix_employee", "grossMonthlyIncome", List.of("1080.0"), SINGLE_VALUE, 1)
        );
    }
}