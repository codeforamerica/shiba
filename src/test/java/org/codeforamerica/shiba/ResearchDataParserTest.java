package org.codeforamerica.shiba;

import org.codeforamerica.shiba.application.FlowType;
import org.codeforamerica.shiba.application.parsers.TotalIncomeParser;
import org.codeforamerica.shiba.output.caf.TotalIncome;
import org.codeforamerica.shiba.output.caf.TotalIncomeCalculator;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.codeforamerica.shiba.pages.data.Subworkflows;
import org.codeforamerica.shiba.research.ResearchData;
import org.codeforamerica.shiba.research.ResearchDataParser;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ResearchDataParserTest {
    TotalIncomeCalculator totalIncomeCalculator = mock(TotalIncomeCalculator.class);
    TotalIncomeParser totalIncomeParser = mock(TotalIncomeParser.class);

    ResearchDataParser researchDataParser = new ResearchDataParser(
            totalIncomeCalculator, totalIncomeParser
    );
    private final PagesDataBuilder pagesDataBuilder = new PagesDataBuilder();

    @Test
    void shouldParseResearchData() {
        ApplicationData applicationData = new ApplicationData();

        PagesData pagesData = pagesDataBuilder.build(List.of(
                new PageDataBuilder("languagePreferences", Map.of(
                        "spokenLanguage", List.of("English"),
                        "writtenLanguage", List.of("Spanish")
                )),
                new PageDataBuilder("personalInfo", Map.of(
                        "sex", List.of("female"),
                        "firstName", List.of("Person"),
                        "lastName", List.of("Fake"),
                        "dateOfBirth", List.of("10", "04", "2020"),
                        "ssn", List.of("123")
                )),
                new PageDataBuilder("contactInfo", Map.of(
                        "phoneNumber", List.of("6038791111"),
                        "email", List.of("fake@email.com"),
                        "phoneOrEmail", List.of("TEXT")
                )),
                new PageDataBuilder("homeAddress", Map.of(
                        "zipCode", List.of("1111-1111"),
                        "enrichedCounty", List.of("someCounty")
                )),
                new PageDataBuilder("choosePrograms", Map.of(
                        "programs", List.of("SNAP", "GRH", "EA")
                )),
                new PageDataBuilder("addHouseholdMembers", Map.of(
                        "addHouseholdMembers", List.of("false")
                )),
                new PageDataBuilder("homeExpensesAmount", Map.of(
                        "homeExpensesAmount", List.of("111.1")
                )),
                new PageDataBuilder("employmentStatus", Map.of(
                        "areYouWorking", List.of("true")
                ))
        ));
        applicationData.setPagesData(pagesData);

        when(totalIncomeCalculator.calculate(new TotalIncome(789.0, emptyList()))).thenReturn(123.0);
        when(totalIncomeParser.parse(any())).thenReturn(new TotalIncome(789.0, emptyList()));

        ResearchData researchData = researchDataParser.parse(applicationData);

        ResearchData expectedResearchData = ResearchData.builder()
                .spokenLanguage("English")
                .writtenLanguage("Spanish")
                .sex("female")
                .firstName("Person")
                .lastName("Fake")
                .dateOfBirth(LocalDate.of(2020, 10, 4))
                .enteredSsn(true)
                .phoneNumber("6038791111")
                .email("fake@email.com")
                .phoneOptIn(true)
                .emailOptIn(false)
                .zipCode("1111-1111")
                .snap(true)
                .cash(false)
                .housing(true)
                .emergency(true)
                .childcare(false)
                .hasHousehold(false)
                .moneyMadeLast30Days(123.0)
                .homeExpensesAmount(111.1)
                .areYouWorking(true)
                .county("someCounty")
                .build();
        assertThat(researchData).isEqualTo(expectedResearchData);
    }

    @Test
    void shouldParseResearchData_withoutOptionalInputs() {
        ApplicationData applicationData = new ApplicationData();

        PagesData pagesData = pagesDataBuilder.build(List.of(
                new PageDataBuilder("languagePreferences", Map.of(
                        "spokenLanguage", List.of("English"),
                        "writtenLanguage", List.of("Spanish")
                )),
                new PageDataBuilder("personalInfo", Map.of(
                        "firstName", List.of("Person"),
                        "lastName", List.of("Fake")
                )),
                new PageDataBuilder("contactInfo", Map.of(
                        "phoneNumber", List.of("6038791111")
                )),
                new PageDataBuilder("homeAddress", Map.of(
                )),
                new PageDataBuilder("choosePrograms", Map.of(
                        "programs", List.of("SNAP", "GRH", "EA", "CCAP")
                )),
                new PageDataBuilder("addHouseholdMembers", Map.of(
                        "addHouseholdMembers", List.of("false")
                )),
                new PageDataBuilder("homeExpensesAmount", Map.of(
                )),
                new PageDataBuilder("employmentStatus", Map.of(
                        "areYouWorking", List.of("true")
                ))
        ));
        applicationData.setPagesData(pagesData);

        when(totalIncomeCalculator.calculate(new TotalIncome(789.0, emptyList()))).thenReturn(123.0);
        when(totalIncomeParser.parse(any())).thenReturn(new TotalIncome(789.0, emptyList()));

        ResearchData researchData = researchDataParser.parse(applicationData);

        ResearchData expectedResearchData = ResearchData.builder()
                .spokenLanguage("English")
                .writtenLanguage("Spanish")
                .sex(null)
                .firstName("Person")
                .lastName("Fake")
                .dateOfBirth(null)
                .enteredSsn(false)
                .phoneNumber("6038791111")
                .email(null)
                .phoneOptIn(false)
                .emailOptIn(false)
                .zipCode(null)
                .snap(true)
                .cash(false)
                .housing(true)
                .emergency(true)
                .childcare(true)
                .hasHousehold(false)
                .moneyMadeLast30Days(123.0)
                .homeExpensesAmount(null)
                .areYouWorking(true)
                .county(null)
                .build();
        assertThat(researchData).isEqualTo(expectedResearchData);
    }

    @Test
    void shouldParseResearchData_whenContactInfoPageDataIsMissing() {
        ApplicationData applicationData = new ApplicationData();
        applicationData.setPagesData(new PagesData());

        ResearchData researchData = researchDataParser.parse(applicationData);

        assertThat(researchData.getEmailOptIn()).isNull();
        assertThat(researchData.getPhoneOptIn()).isNull();
    }

    @Test
    void shouldParseResearchData_withEnteredSsnWhenPersonalInfoPageDataIsMissing() {
        ApplicationData applicationData = new ApplicationData();
        applicationData.setPagesData(new PagesData());

        ResearchData researchData = researchDataParser.parse(applicationData);

        assertThat(researchData.getEnteredSsn()).isNull();
    }

    @Test
    void shouldParseResearchData_whenCountyInfoIsNotPresent() {
        ApplicationData applicationData = new ApplicationData();
        PagesData pagesData = pagesDataBuilder.build(List.of(
                new PageDataBuilder("homeAddress", Map.of(
                        "zipCode", List.of("1111-1111")
                ))
        ));
        applicationData.setPagesData(pagesData);

        ResearchData researchData = researchDataParser.parse(applicationData);

        assertThat(researchData.getCounty()).isNull();
    }

    @Test
    void shouldParseResearchData_forPayMortgage() {
        ApplicationData applicationData = new ApplicationData();
        applicationData.setFlow(FlowType.FULL);

        PagesData pagesData = pagesDataBuilder.build(List.of(
                new PageDataBuilder("expeditedExpenses", Map.of(
                        "payRentOrMortgage", List.of("false")
                )),
                new PageDataBuilder("homeExpenses", Map.of(
                        "homeExpenses", List.of("MORTGAGE")
                ))
        ));
        applicationData.setPagesData(pagesData);

        ResearchData researchData = researchDataParser.parse(applicationData);

        assertThat(researchData.getPayRentOrMortgage()).isTrue();
    }

    @Test
    void shouldParseResearchData_forPayRent() {
        ApplicationData applicationData = new ApplicationData();
        applicationData.setFlow(FlowType.FULL);

        PagesData pagesData = pagesDataBuilder.build(List.of(
                new PageDataBuilder("expeditedExpenses", Map.of(
                        "payRentOrMortgage", List.of("false")
                )),
                new PageDataBuilder("homeExpenses", Map.of(
                        "homeExpenses", List.of("RENT")
                ))
        ));
        applicationData.setPagesData(pagesData);

        ResearchData researchData = researchDataParser.parse(applicationData);

        assertThat(researchData.getPayRentOrMortgage()).isTrue();
    }

    @Test
    void shouldParseResearchData_forPayRentOrMortgage() {
        ApplicationData applicationData = new ApplicationData();
        applicationData.setFlow(FlowType.EXPEDITED);

        PagesData pagesData = pagesDataBuilder.build(List.of(
                new PageDataBuilder("expeditedExpenses", Map.of(
                        "payRentOrMortgage", List.of("true")
                )),
                new PageDataBuilder("homeExpenses", Map.of(
                        "homeExpenses", List.of()
                ))
        ));
        applicationData.setPagesData(pagesData);

        ResearchData researchData = researchDataParser.parse(applicationData);

        assertThat(researchData.getPayRentOrMortgage()).isTrue();
    }

    @Test
    void shouldParseResearchData_forSelfEmployed() {
        ApplicationData applicationData = new ApplicationData();
        Subworkflows subworkflows = applicationData.getSubworkflows();
        subworkflows.addIteration("jobs", pagesDataBuilder.build(List.of(
                new PageDataBuilder("selfEmployment", Map.of("selfEmployment", List.of("false")))
        )));
        subworkflows.addIteration("jobs", pagesDataBuilder.build(List.of(
                new PageDataBuilder("selfEmployment", Map.of("selfEmployment", List.of("true")))
        )));

        ResearchData researchData = researchDataParser.parse(applicationData);

        ResearchData expectedResearchData = ResearchData.builder()
                .selfEmployment(true)
                .build();

        assertThat(researchData).isEqualTo(expectedResearchData);
    }

    @Test
    void shouldParseResearchData_forHouseholdMemberPrograms() {
        ApplicationData applicationData = new ApplicationData();
        Subworkflows subworkflows = applicationData.getSubworkflows();
        subworkflows.addIteration("household", pagesDataBuilder.build(List.of(
                new PageDataBuilder("householdMemberInfo", Map.of("programs", List.of("SNAP")))
        )));
        subworkflows.addIteration("household", pagesDataBuilder.build(List.of(
                new PageDataBuilder("householdMemberInfo", Map.of("programs", List.of("CCAP")))
        )));

        ResearchData researchData = researchDataParser.parse(applicationData);

        ResearchData expectedResearchData = ResearchData.builder()
                .snap(true)
                .childcare(true)
                .cash(false)
                .housing(false)
                .emergency(false)
                .householdSize(3)
                .build();

        assertThat(researchData).isEqualTo(expectedResearchData);
    }

    @Test
    void shouldParseResearchData_selfEmploymentWorkflowIsNull() {
        ApplicationData applicationData = new ApplicationData();

        ResearchData researchData = researchDataParser.parse(applicationData);

        ResearchData expectedResearchData = ResearchData.builder()
                .selfEmployment(null)
                .build();

        assertThat(researchData).isEqualTo(expectedResearchData);
    }

    @Test
    void shouldParseResearchData_forUnearnedIncomeSources() {
        ApplicationData applicationData = new ApplicationData();

        PagesData pagesData = pagesDataBuilder.build(List.of(
                new PageDataBuilder("unearnedIncome", Map.of(
                        "unearnedIncome", List.of("SOCIAL_SECURITY", "UNEMPLOYMENT")))
        ));
        applicationData.setPagesData(pagesData);

        ResearchData researchData = researchDataParser.parse(applicationData);
        ResearchData expectedResearchData = ResearchData.builder()
                .socialSecurity(true)
                .SSI(false)
                .veteransBenefits(false)
                .unemployment(true)
                .workersCompensation(false)
                .retirement(false)
                .childOrSpousalSupport(false)
                .tribalPayments(false)
                .build();

        assertThat(researchData).isEqualTo(expectedResearchData);
    }

    @Test
    void shouldParseResearchData_forFlowType() {
        ApplicationData applicationData = new ApplicationData();
        applicationData.setFlow(FlowType.EXPEDITED);

        ResearchData researchData = researchDataParser.parse(applicationData);

        assertThat(researchData).isEqualTo(ResearchData.builder()
                .flow(FlowType.EXPEDITED)
                .build());
    }

    @Test
    void shouldParseResearchData_forApplicationId() {
        ApplicationData applicationData = new ApplicationData();
        applicationData.setId("someId");

        ResearchData researchData = researchDataParser.parse(applicationData);

        assertThat(researchData).isEqualTo(ResearchData.builder()
                .applicationId("someId")
                .build());
    }

    @Test
    void shouldParseResarchData_forHouseholdSize() {
        ApplicationData applicationData = new ApplicationData();
        Subworkflows subworkflows = applicationData.getSubworkflows();
        subworkflows.addIteration("household", new PagesData());
        subworkflows.addIteration("household", new PagesData());

        ResearchData researchData = researchDataParser.parse(applicationData);

        assertThat(researchData.getHouseholdSize()).isEqualTo(3);
    }

    @Test
    void shouldParseResearchData_forHouseholdNotPresent() {
        ApplicationData applicationData = new ApplicationData();

        ResearchData researchData = researchDataParser.parse(applicationData);
        ResearchData expectedResearchData = ResearchData.builder()
                .householdSize(null)
                .build();

        assertThat(researchData).isEqualTo(expectedResearchData);
    }

    @Test
    void shouldParseResearchData_forBlankDateOfBirth() {
        ApplicationData applicationData = new ApplicationData();

        PagesData pagesData = pagesDataBuilder.build(List.of(
                new PageDataBuilder("languagePreferences", Map.of(
                        "spokenLanguage", List.of("English"),
                        "writtenLanguage", List.of("Spanish")
                )),
                new PageDataBuilder("personalInfo", Map.of(
                        "dateOfBirth", List.of("", "", "")
                ))));

        applicationData.setPagesData(pagesData);

        ResearchData researchData = researchDataParser.parse(applicationData);
        ResearchData expectedResearchData = ResearchData.builder()
                .dateOfBirth(null)
                .build();

        assertThat(researchData.getDateOfBirth()).isEqualTo(expectedResearchData.getDateOfBirth());
    }

    @Test
    void shouldParseResearchData_forHomeExpensesAmountWithCommas() {
        ApplicationData applicationData = new ApplicationData();

        PagesData pagesData = pagesDataBuilder.build(List.of(
                new PageDataBuilder("homeExpensesAmount", Map.of("homeExpensesAmount", List.of("1,200"))))
        );

        applicationData.setPagesData(pagesData);

        ResearchData researchData = researchDataParser.parse(applicationData);
        ResearchData expectedResearchData = ResearchData.builder()
                .homeExpensesAmount(Double.valueOf("1200"))
                .build();

        assertThat(researchData.getHomeExpensesAmount()).isEqualTo(expectedResearchData.getHomeExpensesAmount());
    }

}