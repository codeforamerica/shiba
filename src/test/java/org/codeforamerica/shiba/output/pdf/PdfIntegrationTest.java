package org.codeforamerica.shiba.output.pdf;

import org.codeforamerica.shiba.Application;
import org.codeforamerica.shiba.ApplicationRepository;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.ApplicationInputType;
import org.codeforamerica.shiba.output.applicationinputsmappers.ApplicationInputsMappers;
import org.codeforamerica.shiba.output.applicationinputsmappers.CoverPageInputsMapper;
import org.codeforamerica.shiba.pages.data.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.output.ApplicationInputType.ENUMERATED_SINGLE_VALUE;
import static org.codeforamerica.shiba.output.Recipient.CASEWORKER;

@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
public class PdfIntegrationTest {
    @Autowired
    private ApplicationInputsMappers mappers;

    @MockBean
    CoverPageInputsMapper coverPageInputsMapper;

    @MockBean
    private ApplicationRepository applicationRepository;

    ApplicationData data = new ApplicationData();
    PagesData pagesData = new PagesData();
    private final ZonedDateTime completedAt = ZonedDateTime.now();
    private final Application application = new Application("someId", completedAt, data, null);

    @BeforeEach
    void setUp() {
        data.setPagesData(pagesData);
        PageData homeAddress = new PageData();
        homeAddress.put("zipCode", InputData.builder().value(List.of("")).build());
        homeAddress.put("city", InputData.builder().value(List.of("")).build());
        homeAddress.put("state", InputData.builder().value(List.of("")).build());
        homeAddress.put("streetAddress", InputData.builder().value(List.of("")).build());
        homeAddress.put("apartmentNumber", InputData.builder().value(List.of("")).build());
        homeAddress.put("isHomeless", InputData.builder().value(List.of("")).build());
        homeAddress.put("sameMailingAddress", InputData.builder().value(List.of("")).build());
        pagesData.put("homeAddress", homeAddress);
    }

    @Test
    void shouldMapNoForUnearnedIncomeOptionsThatAreNotChecked() {
        PageData pageData = new PageData();
        pageData.put(
                "unearnedIncome",
                InputData.builder()
                        .value(List.of("SOCIAL_SECURITY", "CHILD_OR_SPOUSAL_SUPPORT"))
                        .build()
        );
        pagesData.putPage("unearnedIncome", pageData);
        data.setPagesData(pagesData);

        List<ApplicationInput> applicationInputs = mappers.map(application, CASEWORKER);

        assertThat(applicationInputs).contains(
                new ApplicationInput("unearnedIncome", "unearnedIncome", List.of("SOCIAL_SECURITY", "CHILD_OR_SPOUSAL_SUPPORT"), ApplicationInputType.ENUMERATED_MULTI_VALUE),
                new ApplicationInput("unearnedIncome", "noSSI", List.of("No"), ENUMERATED_SINGLE_VALUE),
                new ApplicationInput("unearnedIncome", "noVeteransBenefits", List.of("No"), ENUMERATED_SINGLE_VALUE),
                new ApplicationInput("unearnedIncome", "noUnemployment", List.of("No"), ENUMERATED_SINGLE_VALUE),
                new ApplicationInput("unearnedIncome", "noWorkersCompensation", List.of("No"), ENUMERATED_SINGLE_VALUE),
                new ApplicationInput("unearnedIncome", "noRetirement", List.of("No"), ENUMERATED_SINGLE_VALUE),
                new ApplicationInput("unearnedIncome", "noTribalPayments", List.of("No"), ENUMERATED_SINGLE_VALUE)

        );
    }

    @ParameterizedTest
    @CsvSource(value = {
            "true,false,false",
            "true,true,true"
    })
    void shouldAnswerEnergyAssistanceQuestion(
            Boolean hasEnergyAssistance,
            Boolean hasMoreThan20ForEnergyAssistance,
            String result
    ) {
        PageData energyAssistancePageData = new PageData();
        energyAssistancePageData.put(
                "energyAssistance",
                InputData.builder()
                        .value(List.of(hasEnergyAssistance.toString()))
                        .build()
        );

        PageData energyAssistanceMoreThan20PageData = new PageData();
        energyAssistanceMoreThan20PageData.put(
                "energyAssistanceMoreThan20",
                InputData.builder()
                        .value(List.of(hasMoreThan20ForEnergyAssistance.toString()))
                        .build()
        );
        pagesData.putPage("energyAssistance", energyAssistancePageData);
        pagesData.putPage("energyAssistanceMoreThan20", energyAssistanceMoreThan20PageData);

        data.setPagesData(pagesData);

        List<ApplicationInput> applicationInputs = mappers.map(application, CASEWORKER);

        assertThat(applicationInputs).contains(
                new ApplicationInput(
                        "energyAssistanceGroup",
                        "energyAssistanceInput",
                        List.of(result),
                        ENUMERATED_SINGLE_VALUE
                )
        );
    }

    @Test
    void shouldMapEnergyAssistanceWhenUserReceivedNoAssistance() {
        PageData energyAssistancePageData = new PageData();
        energyAssistancePageData.put(
                "energyAssistance",
                InputData.builder()
                        .value(List.of("false"))
                        .build()
        );

        pagesData.putPage("energyAssistance", energyAssistancePageData);

        data.setPagesData(pagesData);

        List<ApplicationInput> applicationInputs = mappers.map(application, CASEWORKER);

        assertThat(applicationInputs).contains(
                new ApplicationInput("energyAssistanceGroup", "energyAssistanceInput", List.of("false"), ENUMERATED_SINGLE_VALUE)
        );
    }

    @Test
    void shouldMapNoForSelfEmployment() {
        Subworkflows subworkflows = new Subworkflows();
        Subworkflow subworkflow = new Subworkflow();
        PagesData pagesData = new PagesData();
        PageData selfEmploymentPageData = new PageData();
        selfEmploymentPageData.put("selfEmployment", InputData.builder().value(List.of("false")).build());
        pagesData.put("selfEmployment", selfEmploymentPageData);
        PageData paidByTheHourPage = new PageData();
        paidByTheHourPage.put("paidByTheHour", InputData.builder().value(List.of("false")).build());
        pagesData.put("paidByTheHour", paidByTheHourPage);
        PageData payPeriod = new PageData();
        payPeriod.put("payPeriod", InputData.builder().value(List.of("EVERY_WEEK")).build());
        pagesData.put("payPeriod", payPeriod);
        PageData payPerPeriod = new PageData();
        payPerPeriod.put("incomePerPayPeriod", InputData.builder().value(List.of("1")).build());
        pagesData.put("incomePerPayPeriod", payPerPeriod);
        subworkflow.add(pagesData);
        subworkflows.put("jobs", subworkflow);
        data.setSubworkflows(subworkflows);

        List<ApplicationInput> applicationInputs = mappers.map(application, CASEWORKER);

        assertThat(applicationInputs).contains(
                new ApplicationInput("employee", "selfEmployed", List.of("false"), ENUMERATED_SINGLE_VALUE)
        );
    }
}
