package org.codeforamerica.shiba.application.parsers;

import org.codeforamerica.shiba.output.caf.CcapExpeditedEligibilityParameters;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.InputData;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CcapExpeditedEligibilityParserTest extends AbstractParserTest {
    private CcapExpeditedEligibilityParser ccapExpeditedEligibilityParser;
    private final ApplicationData applicationData = new ApplicationData();
    private final PagesData pagesData = new PagesData();

    @BeforeEach
    void setUp() {
        applicationData.setPagesData(pagesData);
        ccapExpeditedEligibilityParser = new CcapExpeditedEligibilityParser(parsingConfiguration);
    }

    @Test
    void shouldParseWhenApplicantListsCcap() {
        pagesData.putPage("livingSituation", new PageData(Map.of("livingSituation", InputData.builder().value(List.of("LIVING_IN_A_PLACE_NOT_MEANT_FOR_HOUSING")).build())));
        pagesData.putPage("choosePrograms", new PageData(Map.of("programs", InputData.builder().value(List.of("CCAP")).build())));

        CcapExpeditedEligibilityParameters parameters = ccapExpeditedEligibilityParser.parse(applicationData).get();

        assertThat(parameters).isEqualTo(new CcapExpeditedEligibilityParameters("LIVING_IN_A_PLACE_NOT_MEANT_FOR_HOUSING", true));
    }

    @Test
    void shouldParseWhenHouseholdMemberListsCcap() {
        pagesData.putPage("livingSituation", new PageData(Map.of("livingSituation", InputData.builder().value(List.of("LIVING_IN_A_PLACE_NOT_MEANT_FOR_HOUSING")).build())));

        PagesData householdMemberPagesData = new PagesData();
        householdMemberPagesData.putPage("householdMemberInfo", new PageData(Map.of("programs", InputData.builder().value(List.of("CCAP")).build())));
        applicationData.getSubworkflows().addIteration("household", householdMemberPagesData);

        CcapExpeditedEligibilityParameters parameters = ccapExpeditedEligibilityParser.parse(applicationData).get();

        assertThat(parameters).isEqualTo(new CcapExpeditedEligibilityParameters("LIVING_IN_A_PLACE_NOT_MEANT_FOR_HOUSING", true));
    }

    @Test
    void shouldReturnUnknownWhenEligibilityInputsNotAvailable() {
        CcapExpeditedEligibilityParameters parameters = ccapExpeditedEligibilityParser.parse(applicationData).get();

        assertThat(parameters).isEqualTo(new CcapExpeditedEligibilityParameters("UNKNOWN", false));
    }
}