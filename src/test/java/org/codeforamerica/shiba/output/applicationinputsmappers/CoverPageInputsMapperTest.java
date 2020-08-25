package org.codeforamerica.shiba.output.applicationinputsmappers;

import org.codeforamerica.shiba.Application;
import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.ApplicationInputType;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.InputData;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CoverPageInputsMapperTest {
    private final Map<County, String> countyInstructionsMapping = new HashMap<>();
    private final CoverPageInputsMapper coverPageInputsMapper = new CoverPageInputsMapper(countyInstructionsMapping);

    @BeforeEach
    void setUp() {
        countyInstructionsMapping.put(County.OTHER, "other instructions");
    }

    @Test
    void shouldIncludeProgramsInputWithCombinedProgramSelection() {
        ApplicationData applicationData = new ApplicationData();
        applicationData.setPagesData(new PagesData(Map.of(
                "choosePrograms",
                new PageData(Map.of("programs", InputData.builder()
                        .value(List.of("SNAP", "CASH"))
                        .build())))));
        Application application = new Application("someId", ZonedDateTime.now(), applicationData, County.OTHER);

        List<ApplicationInput> applicationInputs = coverPageInputsMapper.map(application);

        assertThat(applicationInputs).contains(
                new ApplicationInput(
                        "coverPage",
                        "programs",
                        List.of("SNAP, CASH"),
                        ApplicationInputType.SINGLE_VALUE
                ));
    }

    @Test
    void shouldIncludeCountyInstructionsInputWithMatchingCountyInstructions() {
        ApplicationData applicationData = new ApplicationData();
        applicationData.setPagesData(new PagesData(Map.of(
                "choosePrograms",
                new PageData(Map.of("programs", InputData.builder()
                        .value(List.of())
                        .build())))));


        Application application = new Application("someId", ZonedDateTime.now(), applicationData, County.OLMSTED);
        String countyInstructions = "olmsted instructions";
        countyInstructionsMapping.put(County.OLMSTED, countyInstructions);

        List<ApplicationInput> applicationInputs = coverPageInputsMapper.map(application);

        assertThat(applicationInputs).contains(
                new ApplicationInput(
                        "coverPage",
                        "countyInstructions",
                        List.of(countyInstructions),
                        ApplicationInputType.SINGLE_VALUE
                ));
    }
}