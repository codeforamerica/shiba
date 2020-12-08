package org.codeforamerica.shiba.output.applicationinputsmappers;

import org.codeforamerica.shiba.*;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.ApplicationInputType;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.output.caf.CoverPageInputsMapper;
import org.codeforamerica.shiba.pages.data.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@ContextConfiguration(classes = StaticMessageSourceConfiguration.class)
class CoverPageInputsMapperTest extends AbstractBasePageTest {
    private final CountyMap<Map<Recipient, String>> countyInstructionsMapping = new CountyMap<>();
    private CoverPageInputsMapper coverPageInputsMapper;
    private final PagesDataBuilder pagesDataBuilder = new PagesDataBuilder();
    private final StaticMessageSource staticMessageSource = new StaticMessageSource();

    PagesData pagesData = new PagesData();
    ApplicationData applicationData = new ApplicationData();

    @Override
    @BeforeEach
    public void setUp() throws IOException {
        super.setUp();
        applicationData.setPagesData(pagesData);
        coverPageInputsMapper = new CoverPageInputsMapper(countyInstructionsMapping, staticMessageSource);
        countyInstructionsMapping.getCounties().put(County.Other, Map.of(
                Recipient.CLIENT, "county-to-instructions.default-client",
                Recipient.CASEWORKER, "county-to-instructions.default-caseworker"));
        staticMessageSource.addMessage("county-to-instructions.default-client", LocaleContextHolder.getLocale(), "Default client");
        staticMessageSource.addMessage("county-to-instructions.default-caseworker", LocaleContextHolder.getLocale(), "Default caseworker");
        staticMessageSource.addMessage("county-to-instructions.olmsted-caseworker", LocaleContextHolder.getLocale(), "Olmsted caseworker");
        staticMessageSource.addMessage("county-to-instructions.olmsted-client", LocaleContextHolder.getLocale(), "Olmsted client");
    }

    @Test
    void shouldIncludeProgramsInputWithCombinedProgramSelection() {
        pagesData.put("choosePrograms",
                new PageData(Map.of("programs", InputData.builder()
                        .value(List.of("SNAP", "CASH"))
                        .build())));
        Application application = Application.builder()
                .applicationData(applicationData)
                .county(County.Other)
                .build();

        List<ApplicationInput> applicationInputs = coverPageInputsMapper.map(application, Recipient.CLIENT);

        assertThat(applicationInputs).contains(
                new ApplicationInput(
                        "coverPage",
                        "programs",
                        List.of("SNAP, CASH"),
                        ApplicationInputType.SINGLE_VALUE
                ));
    }

    @Test
    void shouldIncludeSubworkflowProgramsInputWithCombinedProgramSelection() {
        PagesData pagesData = pagesDataBuilder.build(List.of(
                new PageDataBuilder("householdMemberInfo", Map.of(
                        "programs", List.of("SNAP", "CASH"),
                        "firstName", List.of("Jane"),
                        "lastName", List.of("Testuser")
                ))
        ));

        Subworkflows subworkflows = new Subworkflows();
        subworkflows.addIteration("household", pagesData);
        applicationData.setSubworkflows(subworkflows);
        Application application = Application.builder()
                .applicationData(applicationData)
                .county(County.Other)
                .build();

        List<ApplicationInput> applicationInputs = coverPageInputsMapper.map(application, Recipient.CLIENT);

        assertThat(applicationInputs).contains(
                new ApplicationInput(
                        "coverPage",
                        "programs",
                        List.of("SNAP, CASH"),
                        ApplicationInputType.SINGLE_VALUE,
                        0
                ));
    }


    @Test
    void shouldIncludeSubworkflowFullNames() {
        PagesData pagesData = pagesDataBuilder.build(List.of(
                new PageDataBuilder("householdMemberInfo", Map.of(
                        "programs", List.of("SNAP", "CASH"),
                        "firstName", List.of("Jane"),
                        "lastName", List.of("Testuser")
                ))
        ));
        Subworkflows subworkflows = new Subworkflows();
        subworkflows.addIteration("household", pagesData);
        applicationData.setSubworkflows(subworkflows);
        Application application = Application.builder()
                .applicationData(applicationData)
                .county(County.Other)
                .build();

        List<ApplicationInput> applicationInputs = coverPageInputsMapper.map(application, Recipient.CLIENT);

        assertThat(applicationInputs).contains(
                new ApplicationInput(
                        "coverPage",
                        "fullName",
                        List.of("Jane Testuser"),
                        ApplicationInputType.SINGLE_VALUE,
                        0
                ));
    }

    @Test
    void shouldNotIncludeProgramsInput_whenThereAreNoChosenPrograms() {
        Application application = Application.builder()
                .applicationData(applicationData)
                .county(County.Other)
                .build();

        List<String> appInputNames = coverPageInputsMapper.map(application, Recipient.CLIENT).stream()
                .map(ApplicationInput::getName)
                .collect(Collectors.toList());

        assertThat(appInputNames).doesNotContain("programs");
    }

    @Test
    void shouldNotIncludeFullNameInput_whenThereIsNoPersonalInfo() {
        Application application = Application.builder()
                .applicationData(applicationData)
                .county(County.Other)
                .build();

        List<String> appInputNames = coverPageInputsMapper.map(application, Recipient.CLIENT).stream()
                .map(ApplicationInput::getName)
                .collect(Collectors.toList());

        assertThat(appInputNames).doesNotContain("fullName");
    }

    @Test
    void shouldIncludeCountyInstructionsInputWithMatchingCountyInstructions() {
        Application application = Application.builder()
                .id("someId")
                .completedAt(ZonedDateTime.now())
                .applicationData(applicationData)
                .county(County.Olmsted)
                .timeToComplete(null)
                .build();
        String clientCountyInstructions = "county-to-instructions.olmsted-caseworker";
        String caseworkerCountyInstructions = "county-to-instructions.olmsted-client";
        countyInstructionsMapping.getCounties().put(County.Olmsted, Map.of(
                Recipient.CLIENT, clientCountyInstructions,
                Recipient.CASEWORKER, caseworkerCountyInstructions
        ));

        List<ApplicationInput> applicationInputs = coverPageInputsMapper.map(application, Recipient.CASEWORKER);

        assertThat(applicationInputs).contains(
                new ApplicationInput(
                        "coverPage",
                        "countyInstructions",
                        List.of(staticMessageSource.getMessage(caseworkerCountyInstructions, null, Locale.US)),
                        ApplicationInputType.SINGLE_VALUE
                ));

        applicationInputs = coverPageInputsMapper.map(application, Recipient.CLIENT);

        assertThat(applicationInputs).contains(
                new ApplicationInput(
                        "coverPage",
                        "countyInstructions",
                        List.of(staticMessageSource.getMessage(clientCountyInstructions, null, Locale.US)),
                        ApplicationInputType.SINGLE_VALUE
                ));
    }

    @Test
    void shouldIncludeCombinedFirstNameAndLastNameInput() {
        pagesData.put(
                "personalInfo", new PageData(Map.of(
                        "firstName", InputData.builder().value(List.of("someFirstName")).build(),
                        "lastName", InputData.builder().value(List.of("someLastName")).build()))
        );
        Application application = Application.builder()
                .id("someId")
                .completedAt(ZonedDateTime.now())
                .applicationData(applicationData)
                .county(County.Other)
                .timeToComplete(null)
                .build();

        List<ApplicationInput> applicationInputs = coverPageInputsMapper.map(application, Recipient.CLIENT);

        assertThat(applicationInputs).contains(
                new ApplicationInput("coverPage", "fullName", List.of("someFirstName someLastName"), ApplicationInputType.SINGLE_VALUE)
        );
    }
}