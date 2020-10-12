package org.codeforamerica.shiba.output.pdf;

import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.ApplicationRepository;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.ApplicationInputType;
import org.codeforamerica.shiba.output.applicationinputsmappers.ApplicationInputsMappers;
import org.codeforamerica.shiba.output.caf.CoverPageInputsMapper;
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
import java.util.Map;

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
    private final Application application = Application.builder()
            .id("someId")
            .completedAt(completedAt)
            .applicationData(data)
            .county(null)
            .timeToComplete(null)
            .build();
    private final PageData homeAddressPageData = new PageData();
    private final PageData mailingAddressPageData = new PageData();
    private final PageData homeAddressValidationPageData = new PageData();
    private final PageData mailingAddressValidationPageData = new PageData();

    @BeforeEach
    void setUp() {
        data.setPagesData(pagesData);

        homeAddressPageData.put("zipCode", InputData.builder().value(List.of("")).build());
        homeAddressPageData.put("enrichedZipCode", InputData.builder().value(List.of("")).build());
        homeAddressPageData.put("city", InputData.builder().value(List.of("")).build());
        homeAddressPageData.put("enrichedCity", InputData.builder().value(List.of("")).build());
        homeAddressPageData.put("state", InputData.builder().value(List.of("")).build());
        homeAddressPageData.put("streetAddress", InputData.builder().value(List.of("")).build());
        homeAddressPageData.put("apartmentNumber", InputData.builder().value(List.of("")).build());
        homeAddressPageData.put("isHomeless", InputData.builder().value(List.of("")).build());
        homeAddressPageData.put("sameMailingAddress", InputData.builder().value(List.of("")).build());
        homeAddressValidationPageData.put("useEnrichedAddress", InputData.builder().value(List.of("")).build());

        mailingAddressPageData.put("zipCode", InputData.builder().value(List.of("")).build());
        mailingAddressPageData.put("city", InputData.builder().value(List.of("")).build());
        mailingAddressPageData.put("state", InputData.builder().value(List.of("")).build());
        mailingAddressPageData.put("streetAddress", InputData.builder().value(List.of("")).build());
        mailingAddressPageData.put("apartmentNumber", InputData.builder().value(List.of("")).build());
        mailingAddressValidationPageData.put("useEnrichedAddress", InputData.builder().value(List.of("")).build());


        pagesData.putPage("homeAddress", homeAddressPageData);
        pagesData.putPage("homeAddressValidation", homeAddressValidationPageData);
        pagesData.putPage("mailingAddress", mailingAddressPageData);
        pagesData.putPage("mailingAddressValidation", mailingAddressValidationPageData);
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
            "false,true,false",
            "false,false,false",
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

    @Test
    void shouldMapOriginalAddressIfHomeAddressDoesNotUseEnrichedAddress() {
        List<String> originalCityValue = List.of("originalCity");
        List<String> originalZipCodeValue = List.of("originalZipCode");
        List<String> originalApartmentNumber = List.of("originalApt");
        List<String> originalState = List.of("originalState");
        homeAddressPageData.putAll(Map.of(
                "city", InputData.builder().value(originalCityValue).build(),
                "zipCode", InputData.builder().value(originalZipCodeValue).build(),
                "apartmentNumber", InputData.builder().value(originalApartmentNumber).build(),
                "state", InputData.builder().value(originalState).build()));
        homeAddressValidationPageData.put("useEnrichedAddress", InputData.builder().value(List.of("false")).build());

        List<ApplicationInput> applicationInputs = mappers.map(application, CASEWORKER);

        assertThat(applicationInputs).contains(
                new ApplicationInput(
                        "homeAddress",
                        "selectedCity",
                        originalCityValue,
                        ApplicationInputType.SINGLE_VALUE
                ),
                new ApplicationInput(
                        "homeAddress",
                        "selectedZipCode",
                        originalZipCodeValue,
                        ApplicationInputType.SINGLE_VALUE
                ),
                new ApplicationInput(
                        "homeAddress",
                        "selectedApartmentNumber",
                        originalApartmentNumber,
                        ApplicationInputType.SINGLE_VALUE
                ),
                new ApplicationInput(
                        "homeAddress",
                        "selectedState",
                        originalState,
                        ApplicationInputType.SINGLE_VALUE
                )
        );
    }

    @Test
    void shouldMapEnrichedAddressIfHomeAddressUsesEnrichedAddress() {
        List<String> enrichedCityValue = List.of("testCity");
        List<String> enrichedZipCodeValue = List.of("testZipCode");
        List<String> enrichedApartmentNumber = List.of("someApt");
        List<String> enrichedState = List.of("someState");
        homeAddressPageData.putAll(Map.of(
                "enrichedCity", InputData.builder().value(enrichedCityValue).build(),
                "enrichedZipCode", InputData.builder().value(enrichedZipCodeValue).build(),
                "enrichedApartmentNumber", InputData.builder().value(enrichedApartmentNumber).build(),
                "enrichedState", InputData.builder().value(enrichedState).build()));
        homeAddressValidationPageData.put("useEnrichedAddress", InputData.builder().value(List.of("true")).build());

        List<ApplicationInput> applicationInputs = mappers.map(application, CASEWORKER);

        assertThat(applicationInputs).contains(
                new ApplicationInput(
                        "homeAddress",
                        "selectedCity",
                        enrichedCityValue,
                        ApplicationInputType.SINGLE_VALUE
                ),
                new ApplicationInput(
                        "homeAddress",
                        "selectedZipCode",
                        enrichedZipCodeValue,
                        ApplicationInputType.SINGLE_VALUE
                ),
                new ApplicationInput(
                        "homeAddress",
                        "selectedApartmentNumber",
                        enrichedApartmentNumber,
                        ApplicationInputType.SINGLE_VALUE
                ),
                new ApplicationInput(
                        "homeAddress",
                        "selectedState",
                        enrichedState,
                        ApplicationInputType.SINGLE_VALUE
                )
        );
    }

    @Test
    void shouldMapOriginalHomeAddressToMailingAddressIfSameMailingAddressIsTrueAndUseEnrichedAddressIsFalse() {
        homeAddressPageData.putAll(Map.of(
                "zipCode", InputData.builder().value(List.of("someZipCode")).build(),
                "city", InputData.builder().value(List.of("someCity")).build(),
                "state", InputData.builder().value(List.of("someState")).build(),
                "streetAddress", InputData.builder().value(List.of("someStreetAddress")).build(),
                "apartmentNumber", InputData.builder().value(List.of("someApartmentNumber")).build(),
                "sameMailingAddress", InputData.builder().value(List.of("true")).build()
        ));

        homeAddressValidationPageData.put("useEnrichedAddress", InputData.builder().value(List.of("false")).build());

        List<ApplicationInput> applicationInputs = mappers.map(application, CASEWORKER);

        assertThat(applicationInputs).contains(
                new ApplicationInput(
                        "mailingAddress",
                        "selectedZipCode",
                        List.of("someZipCode"),
                        ApplicationInputType.SINGLE_VALUE
                ),
                new ApplicationInput(
                        "mailingAddress",
                        "selectedCity",
                        List.of("someCity"),
                        ApplicationInputType.SINGLE_VALUE
                ),
                new ApplicationInput(
                        "mailingAddress",
                        "selectedState",
                        List.of("someState"),
                        ApplicationInputType.SINGLE_VALUE
                ),
                new ApplicationInput(
                        "mailingAddress",
                        "selectedStreetAddress",
                        List.of("someStreetAddress"),
                        ApplicationInputType.SINGLE_VALUE
                ),
                new ApplicationInput(
                        "mailingAddress",
                        "selectedApartmentNumber",
                        List.of("someApartmentNumber"),
                        ApplicationInputType.SINGLE_VALUE
                )
        );
    }

    @Test
    void shouldMapEnrichedHomeAddressToMailingAddressIfSameMailingAddressIsTrueAndUseEnrichedAddressIsTrue() {
        homeAddressPageData.putAll(Map.of(
                "enrichedZipCode", InputData.builder().value(List.of("someZipCode")).build(),
                "enrichedCity", InputData.builder().value(List.of("someCity")).build(),
                "enrichedState", InputData.builder().value(List.of("someState")).build(),
                "enrichedStreetAddress", InputData.builder().value(List.of("someStreetAddress")).build(),
                "enrichedApartmentNumber", InputData.builder().value(List.of("someApartmentNumber")).build(),
                "sameMailingAddress", InputData.builder().value(List.of("true")).build()
        ));
        homeAddressValidationPageData.put("useEnrichedAddress", InputData.builder().value(List.of("true")).build());

        List<ApplicationInput> applicationInputs = mappers.map(application, CASEWORKER);

        assertThat(applicationInputs).contains(
                new ApplicationInput(
                        "mailingAddress",
                        "selectedZipCode",
                        List.of("someZipCode"),
                        ApplicationInputType.SINGLE_VALUE
                ),
                new ApplicationInput(
                        "mailingAddress",
                        "selectedCity",
                        List.of("someCity"),
                        ApplicationInputType.SINGLE_VALUE
                ),
                new ApplicationInput(
                        "mailingAddress",
                        "selectedState",
                        List.of("someState"),
                        ApplicationInputType.SINGLE_VALUE
                ),
                new ApplicationInput(
                        "mailingAddress",
                        "selectedStreetAddress",
                        List.of("someStreetAddress"),
                        ApplicationInputType.SINGLE_VALUE
                ),
                new ApplicationInput(
                        "mailingAddress",
                        "selectedApartmentNumber",
                        List.of("someApartmentNumber"),
                        ApplicationInputType.SINGLE_VALUE
                )
        );
    }

    @Test
    void shouldMapToOriginalMailingAddressIfSameMailingAddressIsFalseAndUseEnrichedAddressIsFalse() {
        mailingAddressPageData.putAll(Map.of(
                "zipCode", InputData.builder().value(List.of("someZipCode")).build(),
                "city", InputData.builder().value(List.of("someCity")).build(),
                "state", InputData.builder().value(List.of("someState")).build(),
                "streetAddress", InputData.builder().value(List.of("someStreetAddress")).build(),
                "apartmentNumber", InputData.builder().value(List.of("someApartmentNumber")).build()
        ));

        mailingAddressValidationPageData.put("useEnrichedAddress", InputData.builder().value(List.of("false")).build());

        homeAddressPageData.put("sameMailingAddress", InputData.builder().value(List.of("false")).build());

        List<ApplicationInput> applicationInputs = mappers.map(application, CASEWORKER);

        assertThat(applicationInputs).contains(
                new ApplicationInput(
                        "mailingAddress",
                        "selectedZipCode",
                        List.of("someZipCode"),
                        ApplicationInputType.SINGLE_VALUE
                ),
                new ApplicationInput(
                        "mailingAddress",
                        "selectedCity",
                        List.of("someCity"),
                        ApplicationInputType.SINGLE_VALUE
                ),
                new ApplicationInput(
                        "mailingAddress",
                        "selectedState",
                        List.of("someState"),
                        ApplicationInputType.SINGLE_VALUE
                ),
                new ApplicationInput(
                        "mailingAddress",
                        "selectedStreetAddress",
                        List.of("someStreetAddress"),
                        ApplicationInputType.SINGLE_VALUE
                ),
                new ApplicationInput(
                        "mailingAddress",
                        "selectedApartmentNumber",
                        List.of("someApartmentNumber"),
                        ApplicationInputType.SINGLE_VALUE
                )
        );
    }

    @Test
    void shouldMapToEnrichedMailingAddressIfSameMailingAddressIsFalseAndUseEnrichedAddressIsTrue() {
        mailingAddressPageData.putAll(Map.of(
                "enrichedZipCode", InputData.builder().value(List.of("someZipCode")).build(),
                "enrichedCity", InputData.builder().value(List.of("someCity")).build(),
                "enrichedState", InputData.builder().value(List.of("someState")).build(),
                "enrichedStreetAddress", InputData.builder().value(List.of("someStreetAddress")).build(),
                "enrichedApartmentNumber", InputData.builder().value(List.of("someApartmentNumber")).build()
        ));

        mailingAddressValidationPageData.put("useEnrichedAddress", InputData.builder().value(List.of("true")).build());

        homeAddressPageData.put("sameMailingAddress", InputData.builder().value(List.of("false")).build());

        List<ApplicationInput> applicationInputs = mappers.map(application, CASEWORKER);

        assertThat(applicationInputs).contains(
                new ApplicationInput(
                        "mailingAddress",
                        "selectedZipCode",
                        List.of("someZipCode"),
                        ApplicationInputType.SINGLE_VALUE
                ),
                new ApplicationInput(
                        "mailingAddress",
                        "selectedCity",
                        List.of("someCity"),
                        ApplicationInputType.SINGLE_VALUE
                ),
                new ApplicationInput(
                        "mailingAddress",
                        "selectedState",
                        List.of("someState"),
                        ApplicationInputType.SINGLE_VALUE
                ),
                new ApplicationInput(
                        "mailingAddress",
                        "selectedStreetAddress",
                        List.of("someStreetAddress"),
                        ApplicationInputType.SINGLE_VALUE
                ),
                new ApplicationInput(
                        "mailingAddress",
                        "selectedApartmentNumber",
                        List.of("someApartmentNumber"),
                        ApplicationInputType.SINGLE_VALUE
                )
        );
    }
}
