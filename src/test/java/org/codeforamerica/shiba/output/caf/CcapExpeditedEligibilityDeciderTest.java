package org.codeforamerica.shiba.output.caf;

import org.codeforamerica.shiba.PagesDataBuilder;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CcapExpeditedEligibilityDeciderTest {
    private final PagesData pagesData = new PagesData();
    private final ApplicationData applicationData = new ApplicationData();

    private final PagesDataBuilder pagesDataBuilder = new PagesDataBuilder();
    CcapExpeditedEligibilityParser ccapExpeditedEligibilityParser = mock(CcapExpeditedEligibilityParser.class);
    CcapExpeditedEligibilityDecider ccapExpeditedEligibilityDecider = new CcapExpeditedEligibilityDecider(ccapExpeditedEligibilityParser);

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
        when(ccapExpeditedEligibilityParser.parse(applicationData)).thenReturn(Optional.of(new CcapExpeditedEligibilityParameters(livingSituation, true)));
        assertThat(ccapExpeditedEligibilityDecider.decide(applicationData)).isEqualTo(expectedDecision);
    }

    @Test
    void shouldBeUndeterminedWhenLivingSituationIsNotAvailable() {
        when(ccapExpeditedEligibilityParser.parse(applicationData)).thenReturn(Optional.of(new CcapExpeditedEligibilityParameters(null, true)));
        assertThat(ccapExpeditedEligibilityDecider.decide(applicationData)).isEqualTo(CcapExpeditedEligibility.UNDETERMINED);
    }

    @Test
    void shouldBeUndeterminedWhenNotCcapApplication() {
        when(ccapExpeditedEligibilityParser.parse(applicationData)).thenReturn(Optional.of(new CcapExpeditedEligibilityParameters("HOTEL_OR_MOTEL", false)));
        assertThat(ccapExpeditedEligibilityDecider.decide(applicationData)).isEqualTo(CcapExpeditedEligibility.UNDETERMINED);
    }

    @Test
    void shouldBeNotEligibleWhenLivingWithFamilyFriendsDueToOtherReasons() {
        when(ccapExpeditedEligibilityParser.parse(applicationData)).thenReturn(Optional.of(new CcapExpeditedEligibilityParameters("TEMPORARILY_WITH_FRIENDS_OR_FAMILY_OTHER_REASONS", true)));
        assertThat(ccapExpeditedEligibilityDecider.decide(applicationData)).isEqualTo(CcapExpeditedEligibility.NOT_ELIGIBLE);
    }

}