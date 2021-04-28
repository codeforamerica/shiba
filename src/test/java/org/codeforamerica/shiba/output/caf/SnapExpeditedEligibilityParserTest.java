package org.codeforamerica.shiba.output.caf;

import org.codeforamerica.shiba.YamlPropertySourceFactory;
import org.codeforamerica.shiba.application.parsers.ApplicationDataParser;
import org.codeforamerica.shiba.application.parsers.SnapExpeditedEligibilityParser;
import org.codeforamerica.shiba.application.parsers.ParsingConfiguration;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.InputData;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

@SpringBootTest(webEnvironment = NONE)
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
class SnapExpeditedEligibilityParserTest {
    @Autowired
    SnapExpeditedEligibilityParser snapExpeditedEligibilityParser;

    @MockBean
    ApplicationDataParser<List<JobIncomeInformation>> grossMonthlyIncomeParser;

    @MockBean
    TotalIncomeCalculator totalIncomeCalculator;

    private final ApplicationData applicationData = new ApplicationData();
    private final PagesData pagesData = new PagesData();
    private List<JobIncomeInformation> jobIncomeInformation;

    @TestConfiguration
    @PropertySource(value = "classpath:test-parsing-config.yaml", factory = YamlPropertySourceFactory.class)
    static class TestPageConfiguration {
        @SuppressWarnings("ConfigurationProperties")
        @Bean
        @ConfigurationProperties(prefix = "test-parsing")
        public ParsingConfiguration parsingConfiguration() {
            return new ParsingConfiguration();
        }
    }

    @BeforeEach
    void setUp() {
        applicationData.setPagesData(pagesData);
        jobIncomeInformation = List.of(mock(JobIncomeInformation.class));
        when(grossMonthlyIncomeParser.parse(any())).thenReturn(jobIncomeInformation);
    }

    @Test
    void shouldParseAllConfiguredExpeditedEligibilityInputs() {
        pagesData.putPage("incomePage", new PageData(Map.of("incomeInput", InputData.builder().value(List.of("1")).build())));
        pagesData.putPage("assetsPage", new PageData(Map.of("assetsInput", InputData.builder().value(List.of("2")).build())));
        pagesData.putPage("migrantWorkerPage", new PageData(Map.of("migrantWorkerInput", InputData.builder().value(List.of("false")).build())));
        pagesData.putPage("housingCostsPage", new PageData(Map.of("housingCostsInput", InputData.builder().value(List.of("3")).build())));
        String utilitySelection = "some utility";
        pagesData.putPage("utilityExpensesSelectionsPage", new PageData(Map.of("utilityExpensesSelectionsInput", InputData.builder().value(List.of(utilitySelection)).build())));
        pagesData.putPage("choosePrograms", new PageData(Map.of("programs", InputData.builder().value(List.of("SNAP")).build())));

        SnapExpeditedEligibilityParameters parameters = snapExpeditedEligibilityParser.parse(applicationData).get();

        assertThat(parameters).isEqualTo(new SnapExpeditedEligibilityParameters(2.0, 1.0, jobIncomeInformation, false, 3.0, List.of(utilitySelection), true));
    }

    @Test
    void shouldUseDefaultValueWhenPageDataIsNotAvailable() {
        pagesData.putPage("incomePage", new PageData(Map.of("incomeInput", InputData.builder().value(List.of("1.0")).build())));
        pagesData.putPage("migrantWorkerPage", new PageData(Map.of("migrantWorkerInput", InputData.builder().value(List.of("false")).build())));
        pagesData.putPage("housingCostsPage", new PageData(Map.of("housingCostsInput", InputData.builder().value(List.of("3")).build())));
        String utilitySelection = "some utility";
        pagesData.putPage("utilityExpensesSelectionsPage", new PageData(Map.of("utilityExpensesSelectionsInput", InputData.builder().value(List.of(utilitySelection)).build())));
        pagesData.putPage("choosePrograms", new PageData(Map.of("programs", InputData.builder().value(List.of("SNAP")).build())));

        SnapExpeditedEligibilityParameters parameters = snapExpeditedEligibilityParser.parse(applicationData).get();

        assertThat(parameters).isEqualTo(new SnapExpeditedEligibilityParameters(100.0, 1.0, jobIncomeInformation, false, 3.0, List.of(utilitySelection), true));
    }

    @Test
    void shouldUseDefaultValueWhenInputValueIsEmpty() {
        pagesData.putPage("incomePage", new PageData(Map.of("incomeInput", InputData.builder().value(List.of("")).build())));
        pagesData.putPage("assetsPage", new PageData(Map.of("assetsInput", InputData.builder().value(List.of("2")).build())));
        pagesData.putPage("migrantWorkerPage", new PageData(Map.of("migrantWorkerInput", InputData.builder().value(List.of("false")).build())));
        pagesData.putPage("housingCostsPage", new PageData(Map.of("housingCostsInput", InputData.builder().value(List.of("3")).build())));
        String utilitySelection = "some utility";
        pagesData.putPage("utilityExpensesSelectionsPage", new PageData(Map.of("utilityExpensesSelectionsInput", InputData.builder().value(List.of(utilitySelection)).build())));
        pagesData.putPage("choosePrograms", new PageData(Map.of("programs", InputData.builder().value(List.of("SNAP")).build())));

        SnapExpeditedEligibilityParameters parameters = snapExpeditedEligibilityParser.parse(applicationData).get();

        assertThat(parameters).isEqualTo(new SnapExpeditedEligibilityParameters(2.0, 200.0, jobIncomeInformation, false, 3.0, List.of(utilitySelection), true));
    }

    @Test
    void shouldReturnEmptyOptionalWhenAnyRequiredPageIsMissing() {
        pagesData.putPage("incomePage", new PageData(Map.of("incomeInput", InputData.builder().value(List.of("1")).build())));
        pagesData.putPage("assetsPage", new PageData(Map.of("assetsInput", InputData.builder().value(List.of("2")).build())));
        pagesData.putPage("housingCostsPage", new PageData(Map.of("housingCostsInput", InputData.builder().value(List.of("3")).build())));
        String utilitySelection = "some utility";
        pagesData.putPage("utilityExpensesSelectionsPage", new PageData(Map.of("utilityExpensesSelectionsInput", InputData.builder().value(List.of(utilitySelection)).build())));
        pagesData.putPage("choosePrograms", new PageData(Map.of("programs", InputData.builder().value(List.of("SNAP")).build())));

        Optional<SnapExpeditedEligibilityParameters> parameters = snapExpeditedEligibilityParser.parse(applicationData);

        assertThat(parameters).isEmpty();
    }
}