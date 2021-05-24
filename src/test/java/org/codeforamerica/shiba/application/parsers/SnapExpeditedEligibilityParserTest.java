package org.codeforamerica.shiba.application.parsers;

import org.codeforamerica.shiba.Money;
import org.codeforamerica.shiba.PageDataBuilder;
import org.codeforamerica.shiba.PagesDataBuilder;
import org.codeforamerica.shiba.output.caf.JobIncomeInformation;
import org.codeforamerica.shiba.output.caf.SnapExpeditedEligibilityParameters;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.InputData;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.codeforamerica.shiba.pages.data.Subworkflows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SnapExpeditedEligibilityParserTest extends AbstractParserTest {
    private SnapExpeditedEligibilityParser snapExpeditedEligibilityParser;
    private ApplicationData applicationData;
    private PagesData pagesData;
    private List<JobIncomeInformation> jobIncomeInformation;
    private PagesDataBuilder pagesDataBuilder = new PagesDataBuilder();

    @BeforeEach
    void setUp() {
        applicationData = new ApplicationData();
        pagesData = new PagesData();
        applicationData.setPagesData(pagesData);

        var grossMonthlyIncomeParser = mock(GrossMonthlyIncomeParser.class);
        jobIncomeInformation = List.of(mock(JobIncomeInformation.class));
        when(grossMonthlyIncomeParser.parse(any())).thenReturn(jobIncomeInformation);

        snapExpeditedEligibilityParser = new SnapExpeditedEligibilityParser(parsingConfiguration, grossMonthlyIncomeParser);
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

        assertThat(parameters).isEqualTo(new SnapExpeditedEligibilityParameters(Money.parse("2"), Money.ONE, jobIncomeInformation, false, Money.parse("3"), List.of(utilitySelection), true));
    }

    @Test
    void shouldParseExpeditedEligibilityInputsWhenHouseholdMemberAppliesForSnap() {
        pagesData.putPage("incomePage", new PageData(Map.of("incomeInput", InputData.builder().value(List.of("1")).build())));
        pagesData.putPage("assetsPage", new PageData(Map.of("assetsInput", InputData.builder().value(List.of("2")).build())));
        pagesData.putPage("migrantWorkerPage", new PageData(Map.of("migrantWorkerInput", InputData.builder().value(List.of("false")).build())));
        pagesData.putPage("housingCostsPage", new PageData(Map.of("housingCostsInput", InputData.builder().value(List.of("3")).build())));
        String utilitySelection = "some utility";
        pagesData.putPage("utilityExpensesSelectionsPage", new PageData(Map.of("utilityExpensesSelectionsInput", InputData.builder().value(List.of(utilitySelection)).build())));
        pagesData.putPage("choosePrograms", new PageData(Map.of("programs", InputData.builder().value(List.of("GRH")).build())));
        pagesData.putPage("preparingMealsTogether", new PageData(Map.of("isPreparingMealsTogether", InputData.builder().value(List.of("true")).build())));
        
        Subworkflows subworkflows = new Subworkflows();
        PagesData pagesData = pagesDataBuilder.build(List.of(
                new PageDataBuilder("householdMemberInfo", Map.of(
                        "programs", List.of("SNAP")
                ))
        ));
        subworkflows.addIteration("household", pagesData);
        applicationData.setSubworkflows(subworkflows);
        
        SnapExpeditedEligibilityParameters parameters = snapExpeditedEligibilityParser.parse(applicationData).get();

        assertThat(parameters).isEqualTo(new SnapExpeditedEligibilityParameters(Money.parse("2"), Money.ONE, jobIncomeInformation, false, Money.parse("3"), List.of(utilitySelection), true));
    }

    @Test
    void shouldUseDefaultValueWhenPageDataIsNotAvailable() {
        pagesData.putPage("incomePage", new PageData(Map.of("incomeInput", InputData.builder().value(List.of("1")).build())));
        pagesData.putPage("migrantWorkerPage", new PageData(Map.of("migrantWorkerInput", InputData.builder().value(List.of("false")).build())));
        pagesData.putPage("housingCostsPage", new PageData(Map.of("housingCostsInput", InputData.builder().value(List.of("3")).build())));
        String utilitySelection = "some utility";
        pagesData.putPage("utilityExpensesSelectionsPage", new PageData(Map.of("utilityExpensesSelectionsInput", InputData.builder().value(List.of(utilitySelection)).build())));
        pagesData.putPage("choosePrograms", new PageData(Map.of("programs", InputData.builder().value(List.of("SNAP")).build())));

        SnapExpeditedEligibilityParameters parameters = snapExpeditedEligibilityParser.parse(applicationData).get();

        assertThat(parameters).isEqualTo(new SnapExpeditedEligibilityParameters(Money.parse("100"), Money.ONE, jobIncomeInformation, false, Money.parse("3"), List.of(utilitySelection), true));
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

        assertThat(parameters).isEqualTo(new SnapExpeditedEligibilityParameters(Money.parse("2"), Money.parse("200"), jobIncomeInformation, false, Money.parse("3"), List.of(utilitySelection), true));
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