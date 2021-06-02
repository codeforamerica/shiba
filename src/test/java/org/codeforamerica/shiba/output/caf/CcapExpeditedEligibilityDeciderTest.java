package org.codeforamerica.shiba.output.caf;

import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.TestApplicationDataBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CcapExpeditedEligibilityDeciderTest {
    private final CcapExpeditedEligibilityDecider ccapExpeditedEligibilityDecider = new CcapExpeditedEligibilityDecider();

    @ParameterizedTest
    @CsvSource(value = {
            "HOTEL_OR_MOTEL,ELIGIBLE",
            "TEMPORARILY_WITH_FRIENDS_OR_FAMILY_DUE_TO_ECONOMIC_HARDSHIP,ELIGIBLE",
            "TEMPORARILY_WITH_FRIENDS_OR_FAMILY_OTHER_REASONS,NOT_ELIGIBLE",
            "EMERGENCY_SHELTER,ELIGIBLE",
            "LIVING_IN_A_PLACE_NOT_MEANT_FOR_HOUSING,ELIGIBLE",
            "PAYING_FOR_HOUSING_WITH_RENT_LEASE_OR_MORTGAGE,NOT_ELIGIBLE",
            "FOSTER_CARE_OR_GROUP_HOME,NOT_ELIGIBLE",
            "HOSPITAL_OR_OTHER_TREATMENT_FACILITY,NOT_ELIGIBLE",
            "JAIL_OR_JUVENILE_DETENTION_FACILITY,NOT_ELIGIBLE",
            "UNKNOWN,NOT_ELIGIBLE",
            "PREFER_NOT_TO_SAY,NOT_ELIGIBLE",
    })
    void shouldQualifyWhenLivingSituationIsEligible(
            String livingSituation,
            CcapExpeditedEligibility expectedDecision
    ) {
        ApplicationData applicationData = createApplicationData(List.of(livingSituation), "CCAP");
        assertThat(ccapExpeditedEligibilityDecider.decide(applicationData)).isEqualTo(expectedDecision);
    }

    @Test
    void shouldBeUndeterminedWhenLivingSituationIsNotAvailable() {
        ApplicationData applicationData = createApplicationData(Collections.emptyList(), "CCAP");
        assertThat(ccapExpeditedEligibilityDecider.decide(applicationData)).isEqualTo(CcapExpeditedEligibility.UNDETERMINED);
    }

    @Test
    void shouldBeUndeterminedWhenNotCcapApplication() {
        ApplicationData applicationData = createApplicationData(List.of("HOTEL_OR_MOTEL"), "EA");
        assertThat(ccapExpeditedEligibilityDecider.decide(applicationData)).isEqualTo(CcapExpeditedEligibility.UNDETERMINED);
    }

    @Test
    void shouldBeNotEligibleWhenLivingWithFamilyFriendsDueToOtherReasons() {
        ApplicationData applicationData = createApplicationData(List.of("TEMPORARILY_WITH_FRIENDS_OR_FAMILY_OTHER_REASONS"), "CCAP");
        assertThat(ccapExpeditedEligibilityDecider.decide(applicationData)).isEqualTo(CcapExpeditedEligibility.NOT_ELIGIBLE);
    }

    private ApplicationData createApplicationData(List<String> livingSituation, String program) {
        return new TestApplicationDataBuilder()
                .withApplicantPrograms(List.of(program))
                .withPageData("livingSituation", "livingSituation", livingSituation)
                .build();
    }
}