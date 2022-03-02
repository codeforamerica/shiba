package org.codeforamerica.shiba.output.caf;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.testutilities.PagesDataBuilder;
import org.codeforamerica.shiba.testutilities.TestApplicationDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CcapExpeditedEligibilityDeciderTest {

  private final CcapExpeditedEligibilityDecider ccapExpeditedEligibilityDecider = new CcapExpeditedEligibilityDecider();
  private TestApplicationDataBuilder applicationDataBuilder;

  @BeforeEach
  void setup() {
    // Initialize with eligible snap
    applicationDataBuilder = new TestApplicationDataBuilder()
        .withApplicantPrograms(List.of("CCAP"))
        .withSubworkflow("household",
            new PagesDataBuilder().withPageData("householdMemberInfo", Map.of(
                "firstName", "Jane",
                "lastName", "Testerson",
                "dateOfBirth", List.of("01", "09", "2020"))).build())
        ;
  }

  @ParameterizedTest
  @CsvSource(value = {
      "HOTEL_OR_MOTEL,ELIGIBLE",
      "TEMPORARILY_WITH_FRIENDS_OR_FAMILY_DUE_TO_ECONOMIC_HARDSHIP,ELIGIBLE",
      "TEMPORARILY_WITH_FRIENDS_OR_FAMILY_OTHER_REASONS,ELIGIBLE",
      "EMERGENCY_SHELTER,ELIGIBLE",
      "LIVING_IN_A_PLACE_NOT_MEANT_FOR_HOUSING,ELIGIBLE",
      "PAYING_FOR_HOUSING_WITH_RENT_LEASE_OR_MORTGAGE,NOT_ELIGIBLE",
      "FOSTER_CARE_OR_GROUP_HOME,ELIGIBLE",
      "HOSPITAL_OR_OTHER_TREATMENT_FACILITY,ELIGIBLE",
      "JAIL_OR_JUVENILE_DETENTION_FACILITY,ELIGIBLE",
      "UNKNOWN,ELIGIBLE",
      "PREFER_NOT_TO_SAY,ELIGIBLE",
  })
  void shouldQualifyWhenLivingSituationIsEligible(
      String livingSituation,
      CcapExpeditedEligibility expectedDecision
  ) {
    ApplicationData applicationData = applicationDataBuilder
        .withPageData("livingSituation","livingSituation", livingSituation)
        .withPageData("millionDollar", "haveMillionDollars", List.of("false"))
        .build();
    assertThat(ccapExpeditedEligibilityDecider.decide(applicationData)).isEqualTo(expectedDecision);
  }

  @Test
  void shouldBeUndeterminedWhenLivingSituationIsNotAvailable() {
    ApplicationData applicationData = applicationDataBuilder.withPageData("livingSituation",
        "livingSituation", List.of()).build();
    assertThat(ccapExpeditedEligibilityDecider.decide(applicationData))
        .isEqualTo(CcapExpeditedEligibility.UNDETERMINED);
  }

  @Test
  void shouldBeUndeterminedWhenNotCcapApplication() {
    ApplicationData applicationData = createApplicationData(List.of("HOTEL_OR_MOTEL"), "EA");
    assertThat(ccapExpeditedEligibilityDecider.decide(applicationData))
        .isEqualTo(CcapExpeditedEligibility.UNDETERMINED);
  }

  @Test
  void ShouldNotQualifyIfThereAreNoHouseholdMembersUnder12() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withApplicantPrograms(List.of("CCAP"))
        .withPageData("livingSituation", "livingSituation", "EMERGENCY_SHELTER")
        .withPageData("millionDollar", "haveMillionDollars", List.of("false"))
        .withSubworkflow("household",
            new PagesDataBuilder().withPageData("householdMemberInfo", Map.of(
                "firstName", "Jane",
                "lastName", "Testerson",
                "dateOfBirth", List.of("01", "09", "1987"))),
            new PagesDataBuilder().withPageData("householdMemberInfo", Map.of(
                "firstName", "John",
                "lastName", "Testerson",
                "dateOfBirth", List.of("01", "09", "1999")))).build();

    assertThat(ccapExpeditedEligibilityDecider.decide(applicationData))
        .isEqualTo(CcapExpeditedEligibility.NOT_ELIGIBLE);
  }

  @Test
  void shouldOnlyQualifyIfAtLeastOneHouseholdMemberIsAge12OrUnder() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withApplicantPrograms(List.of("CCAP"))
        .withPageData("livingSituation", "livingSituation", "EMERGENCY_SHELTER")
        .withPageData("millionDollar", "haveMillionDollars", List.of("false"))
        .withSubworkflow("household",
            new PagesDataBuilder().withPageData("householdMemberInfo", Map.of(
                "firstName", "Jane",
                "lastName", "Testerson",
                "dateOfBirth", List.of("01", "09", "2020"))),
            new PagesDataBuilder().withPageData("householdMemberInfo", Map.of(
                "firstName", "John",
                "lastName", "Testerson",
                "dateOfBirth", List.of("01", "09", "1999")))).build();

    assertThat(ccapExpeditedEligibilityDecider.decide(applicationData))
        .isEqualTo(CcapExpeditedEligibility.ELIGIBLE);
  }
  
  @Test
  void hasMillionDollarAssetAnsweredYes() {
    ApplicationData applicationData = applicationDataBuilder
        .withPageData("livingSituation", "livingSituation", "EMERGENCY_SHELTER")
        .withPageData("millionDollar", "haveMillionDollars", List.of("true")).build();
    assertThat(ccapExpeditedEligibilityDecider.decide(applicationData))
    .isEqualTo(CcapExpeditedEligibility.NOT_ELIGIBLE);
  }
  
  @Test
  void hasMillionDollarAssetAnsweredNo() {
    ApplicationData applicationData = applicationDataBuilder
        .withPageData("livingSituation", "livingSituation", "EMERGENCY_SHELTER")
        .withPageData("millionDollar", "haveMillionDollars", List.of("false")).build();
    assertThat(ccapExpeditedEligibilityDecider.decide(applicationData))
    .isEqualTo(CcapExpeditedEligibility.ELIGIBLE);
  }
  
  @Test
  void hasMillionDollarAssetNotAnswered() {
    ApplicationData applicationData = applicationDataBuilder
        .withPageData("livingSituation", "livingSituation", "EMERGENCY_SHELTER")
        .withPageData("millionDollar", "haveMillionDollars", List.of()).build();
    assertThat(ccapExpeditedEligibilityDecider.decide(applicationData))
    .isEqualTo(CcapExpeditedEligibility.UNDETERMINED);
  }

  private ApplicationData createApplicationData(List<String> livingSituation, String program) {
    return new TestApplicationDataBuilder()
        .withApplicantPrograms(List.of(program))
        .withPageData("livingSituation", "livingSituation", livingSituation)
        .build();
  }
}
