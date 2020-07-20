package org.codeforamerica.shiba.output.caf;

import org.codeforamerica.shiba.YamlPropertySourceFactory;
import org.codeforamerica.shiba.pages.FormData;
import org.codeforamerica.shiba.pages.InputData;
import org.codeforamerica.shiba.pages.PagesData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = {"spring.main.allow-bean-definition-overriding=true"})
@ExtendWith(SpringExtension.class)
class ExpeditedEligibilityDeciderTest {
    private final PagesData pagesData = new PagesData();

    @MockBean
    UtilityDeductionCalculator mockUtilityDeductionCalculator;

    @Autowired
    ExpeditedEligibilityDecider decider;

    @TestConfiguration
    @PropertySource(value = "classpath:test-expedited-eligibility-config.yaml", factory = YamlPropertySourceFactory.class)
    static class TestPageConfiguration {
        @Bean
        @ConfigurationProperties(prefix = "test-expedited-eligibility")
        public Map<String, PageInputCoordinates> expeditedEligibilityConfiguration() {
            return new HashMap<>();
        }
    }

    @BeforeEach
    void setup() {
        pagesData.putPage("incomePage", new FormData(Map.of("incomeInput", new InputData(List.of("99999")))));
        pagesData.putPage("assetsPage", new FormData(Map.of("assetsInput", new InputData(List.of("99999")))));
        pagesData.putPage("migrantWorkerPage", new FormData(Map.of("migrantWorkerInput", new InputData(List.of("false")))));
        pagesData.putPage("housingCostsPage", new FormData(Map.of("housingCostsInput", new InputData(List.of("99")))));
        pagesData.putPage("utilityExpensesSelectionsPage", new FormData(Map.of("utilityExpensesSelectionsInput", new InputData(List.of()))));
        when(mockUtilityDeductionCalculator.calculate(any())).thenReturn(0);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "149,100,true",
            "151,100,false",
            "149,101,false",
            "150,101,false",
    })
    void shouldQualify_whenMeetingIncomeAndAssetsThresholds(
            String income,
            String assets,
            boolean expectedDecision
    ) {
        pagesData.putPage("incomePage", new FormData(Map.of("incomeInput", new InputData(List.of(income)))));
        pagesData.putPage("assetsPage", new FormData(Map.of("assetsInput", new InputData(List.of(assets)))));

        assertThat(decider.decide(pagesData)).isEqualTo(expectedDecision);
    }

    @Test
    void shouldUseTheConfiguredDefaultValueWhenPageInputDataNotAvailable() {
        pagesData.remove("assetsPage");
        pagesData.putPage("incomePage", new FormData(Map.of("incomeInput", new InputData(List.of("149")))));

        assertThat(decider.decide(pagesData)).isEqualTo(true);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "100,true,true",
            "101,true,false",
            "100,false,false",
            "101,false,false"
    })
    void shouldQualify_whenApplicantIsMigrantWorkerAndMeetAssetThreshold(
            String assets,
            String isMigrantWorker,
            boolean expectedDecision
    ) {
        pagesData.putPage("assetsPage", new FormData(Map.of("assetsInput", new InputData(List.of(assets)))));
        pagesData.putPage("migrantWorkerPage", new FormData(Map.of("migrantWorkerInput", new InputData(List.of(isMigrantWorker)))));

        assertThat(decider.decide(pagesData)).isEqualTo(expectedDecision);
    }

    @Test
    void shouldQualify_whenIncomeAndAssetsAreLessThanExpenses() {
        String income = "500";
        String assets = "1000";
        String rentMortgageAmount = "500";
        when(mockUtilityDeductionCalculator.calculate(any())).thenReturn(1001);

        pagesData.putPage("incomePage", new FormData(Map.of("incomeInput", new InputData(List.of(income)))));
        pagesData.putPage("assetsPage", new FormData(Map.of("assetsInput", new InputData(List.of(assets)))));
        pagesData.putPage("housingCostsPage", new FormData(Map.of("housingCostsInput", new InputData(List.of(rentMortgageAmount)))));

        assertThat(decider.decide(pagesData)).isEqualTo(true);
    }

    @Test
    void shouldNotQualify_whenIncomeAndAssetsAreGreaterThanOrEqualToExpenses_andNotMeetingOtherCriteria() {
        String income = "500";
        String assets = "1000";
        String rentMortgageAmount = "500";
        when(mockUtilityDeductionCalculator.calculate(any())).thenReturn(1000);

        pagesData.putPage("incomePage", new FormData(Map.of("incomeInput", new InputData(List.of(income)))));
        pagesData.putPage("assetsPage", new FormData(Map.of("assetsInput", new InputData(List.of(assets)))));
        pagesData.putPage("housingCostsPage", new FormData(Map.of("housingCostsInput", new InputData(List.of(rentMortgageAmount)))));

        assertThat(decider.decide(pagesData)).isEqualTo(false);
    }

    @Test
    void shouldNotQualify_whenNeededDataIsNotPresent() {
        assertThat(decider.decide(new PagesData())).isEqualTo(false);
    }
}