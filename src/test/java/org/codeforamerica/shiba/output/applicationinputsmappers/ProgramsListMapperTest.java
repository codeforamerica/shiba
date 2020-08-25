package org.codeforamerica.shiba.output.applicationinputsmappers;

import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.ApplicationInputType;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.InputData;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ProgramsListMapperTest {
    private final ProgramsListMapper programsListMapper = new ProgramsListMapper();

    @Test
    void shouldCombineTheProgramsWithAComma() {
        ApplicationData applicationData = new ApplicationData();
        applicationData.setPagesData(new PagesData(Map.of(
                "choosePrograms",
                new PageData(Map.of("programs", InputData.builder()
                        .value(List.of("SNAP", "CASH"))
                        .build())))));

        List<ApplicationInput> applicationInputs = programsListMapper.map(applicationData);

        assertThat(applicationInputs).contains(
                new ApplicationInput(
                        "coverPage",
                        "programs",
                        List.of("SNAP, CASH"),
                        ApplicationInputType.SINGLE_VALUE
                ));
    }
}