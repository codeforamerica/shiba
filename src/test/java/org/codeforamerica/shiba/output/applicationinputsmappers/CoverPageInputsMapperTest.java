package org.codeforamerica.shiba.output.applicationinputsmappers;

import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.CountyMap;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.ApplicationInputType;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.output.caf.CoverPageInputsMapper;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.InputData;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class CoverPageInputsMapperTest {
    private final CountyMap<Map<Recipient, String>> countyInstructionsMapping = new CountyMap<>();
    private final CoverPageInputsMapper coverPageInputsMapper = new CoverPageInputsMapper(countyInstructionsMapping);

    PagesData pagesData = new PagesData();
    ApplicationData applicationData = new ApplicationData();

    @BeforeEach
    void setUp() {
        applicationData.setPagesData(pagesData);
        countyInstructionsMapping.getCounties().put(County.Other, Map.of(
                Recipient.CLIENT, "other client instructions",
                Recipient.CASEWORKER, "other caseworker instructions"));
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
        String clientCountyInstructions = "olmsted client instructions";
        String caseworkerCountyInstructions = "olmsted caseworker instructions";
        countyInstructionsMapping.getCounties().put(County.Olmsted, Map.of(
                Recipient.CLIENT, clientCountyInstructions,
                Recipient.CASEWORKER, caseworkerCountyInstructions
        ));

        List<ApplicationInput> applicationInputs = coverPageInputsMapper.map(application, Recipient.CASEWORKER);

        assertThat(applicationInputs).contains(
                new ApplicationInput(
                        "coverPage",
                        "countyInstructions",
                        List.of(caseworkerCountyInstructions),
                        ApplicationInputType.SINGLE_VALUE
                ));

        applicationInputs = coverPageInputsMapper.map(application, Recipient.CLIENT);

        assertThat(applicationInputs).contains(
                new ApplicationInput(
                        "coverPage",
                        "countyInstructions",
                        List.of(clientCountyInstructions),
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