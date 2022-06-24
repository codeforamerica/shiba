package org.codeforamerica.shiba.output.caf;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.output.caf.CcapExpeditedEligibility.ELIGIBLE;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    ApplicationData applicationData = applicationDataBuilder
        .withPageData("livingSituation", "livingSituation", livingSituation)
        .withPageData("assets", "assets", List.of(""))
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
  void shouldNotQualifyIfThereAreNoHouseholdMembersUnder12() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withApplicantPrograms(List.of("CCAP"))
        .withPageData("livingSituation", "livingSituation", "EMERGENCY_SHELTER")
        .withPageData("assets", "assets", List.of(""))
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
  void blankDobShouldNotAffectEligibility() {
    ApplicationData ineligibleApplication = new TestApplicationDataBuilder()
        .withApplicantPrograms(List.of("CCAP"))
        .withPageData("livingSituation", "livingSituation", "EMERGENCY_SHELTER")
        .withPageData("assets", "assets", List.of(""))
        .withSubworkflow("household",
            new PagesDataBuilder().withPageData("householdMemberInfo", Map.of(
                "firstName", "Jane",
                "lastName", "Testerson",
                "dateOfBirth", List.of("", "", "")))).build();

    assertThat(ccapExpeditedEligibilityDecider.decide(ineligibleApplication))
        .isEqualTo(CcapExpeditedEligibility.NOT_ELIGIBLE);

    LocalDate today = LocalDate.now();
    ApplicationData eligibleApplication = applicationDataBuilder
        .withPageData("livingSituation", "livingSituation", "HOTEL_OR_MOTEL")
        .withPageData("assets", "assets", List.of(""))
        .withSubworkflow("household",
            new PagesDataBuilder().withPageData("householdMemberInfo", Map.of(
                "firstName", "Jane",
                "lastName", "Testerson",
                "dateOfBirth", List.of(
                    String.valueOf(today.getMonthValue()),
                    String.valueOf(today.getDayOfMonth()),
                    String.valueOf(today.getYear()))
            ))).build();
    assertThat(ccapExpeditedEligibilityDecider.decide(eligibleApplication))
        .isEqualTo(CcapExpeditedEligibility.ELIGIBLE);
  }

  @Test
  void shouldOnlyQualifyIfAtLeastOneHouseholdMemberIsAge12OrUnder() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withApplicantPrograms(List.of("CCAP"))
        .withPageData("livingSituation", "livingSituation", "EMERGENCY_SHELTER")
        .withPageData("assets", "assets", List.of(""))
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
        .isEqualTo(ELIGIBLE);
  }

  @Test
  void shouldQualifyIfHouseholdMemberIsOver12YearsButUnder13Years() {
    LocalDateTime twelveYearsAndElevenMonthsAgo = LocalDateTime.now().minusYears(12)
        .minusMonths(11);
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    String date = formatter.format(twelveYearsAndElevenMonthsAgo);
    String[] monthDayYear = date.split("/");
    String month = monthDayYear[0];
    String day = monthDayYear[1];
    String year = monthDayYear[2];

    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withApplicantPrograms(List.of("CCAP"))
        .withPageData("livingSituation", "livingSituation", "EMERGENCY_SHELTER")
        .withPageData("assets", "assets", List.of(""))
        .withSubworkflow("household",
            new PagesDataBuilder().withPageData("householdMemberInfo", Map.of(
                "firstName", "Jane",
                "lastName", "Testerson",
                "dateOfBirth", List.of(month, day, year))),
            new PagesDataBuilder().withPageData("householdMemberInfo", Map.of(
                "firstName", "John",
                "lastName", "Testerson",
                "dateOfBirth", List.of("01", "09", "1999")))).build();

    assertThat(ccapExpeditedEligibilityDecider.decide(applicationData))
        .isEqualTo(ELIGIBLE);
  }

  @Test
  void hasMillionDollarAssetAnsweredYes() {
    ApplicationData applicationData = applicationDataBuilder
        .withPageData("livingSituation", "livingSituation", "EMERGENCY_SHELTER")
        .withPageData("assets", "assets", List.of("ONE_MILLION_ASSETS")).build();
    assertThat(ccapExpeditedEligibilityDecider.decide(applicationData))
        .isEqualTo(CcapExpeditedEligibility.NOT_ELIGIBLE);
  }

  @Test
  void hasMillionDollarAssetAnsweredNo() {
    ApplicationData applicationData = applicationDataBuilder
        .withPageData("livingSituation", "livingSituation", "EMERGENCY_SHELTER")
        .withPageData("assets", "assets", List.of("VEHICLE")).build();
    assertThat(ccapExpeditedEligibilityDecider.decide(applicationData))
        .isEqualTo(ELIGIBLE);
  }

  @Test
  void hasMillionDollarAssetNotAnswered() {
    ApplicationData applicationData = applicationDataBuilder
        .withPageData("livingSituation", "livingSituation", "EMERGENCY_SHELTER")
        .withPageData("assets", "assets", List.of()).build();
    assertThat(ccapExpeditedEligibilityDecider.decide(applicationData))
        .isEqualTo(ELIGIBLE);
  }

  private ApplicationData createApplicationData(List<String> livingSituation, String program) {
    return new TestApplicationDataBuilder()
        .withApplicantPrograms(List.of(program))
        .withPageData("livingSituation", "livingSituation", livingSituation)
        .build();
  }
}
