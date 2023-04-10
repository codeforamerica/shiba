package org.codeforamerica.shiba.pages.enrichment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.codeforamerica.shiba.testutilities.TestApplicationDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DateOfBirthEnrichmentTest {

  private ApplicationData applicationData;

  @BeforeEach
  void setup() {
    applicationData = new TestApplicationDataBuilder()
        .withPageData("personalInfo", "dateOfBirth", List.of("01", "09", "1999"))
        .withPageData("matchInfo", "dateOfBirth", List.of("02", "10", "1999"))
        .withPageData("healthcareRenewalMatchInfo", "dateOfBirth", List.of("02", "10", "1999"))
        .withHouseholdMember("Daria", "Agàta")
        .build();
  }

  @Test
  void dobAsDateIsPresentInPersonalInfoDateOfBirthEnrichment() {
    DateOfBirthEnrichment personalInfoDataOfBirthEnrichment = new PersonalInfoDateOfBirthEnrichment();
    PageData enrichmentResult = personalInfoDataOfBirthEnrichment
        .process(applicationData.getPagesData());

    assertNotNull(enrichmentResult.get("dobAsDate"));
    assertEquals("01/09/1999", enrichmentResult.get("dobAsDate").getValue(0));
  }

  @Test
  void dobAsDateIsPresentInMatchInfoDateOfBirthEnrichment() {
    DateOfBirthEnrichment enrichment = new MatchInfoDateOfBirthEnrichment();

    PageData enrichmentResult = enrichment.process(applicationData.getPagesData());

    assertNotNull(enrichmentResult.get("dobAsDate"));
    assertEquals("02/10/1999", enrichmentResult.get("dobAsDate").getValue(0));
  }

  @Test
  void dobAsDateIsPresentInHouseholdMemberInfoDateOfBirthEnrichment() {
    DateOfBirthEnrichment enrichment = new HouseholdMemberDateOfBirthEnrichment();

    PagesData householdPagesData = applicationData.getSubworkflows().get("household").get(0)
        .getPagesData();
    PageData enrichmentResult = enrichment.process(householdPagesData);

    assertNotNull(enrichmentResult.get("dobAsDate"));
    assertEquals("05/06/1978", enrichmentResult.get("dobAsDate").getValue(0));
  }

  @Test
  void dobAsDateIsEmptyIfDateOfBirthIsEmpty() {
    PageData emptyEnrichmentResult = new PersonalInfoDateOfBirthEnrichment().process(
        new PagesData());

    assertNotNull(emptyEnrichmentResult.get("dobAsDate"));
    assertEquals("", emptyEnrichmentResult.get("dobAsDate").getValue(0));

    PageData blankEnrichmentResult = new PersonalInfoDateOfBirthEnrichment().process(
        new TestApplicationDataBuilder().withPageData(
            "personalInfo", "dateOfBirth", List.of("", "", "")).build().getPagesData());

    assertNotNull(blankEnrichmentResult.get("dobAsDate"));
    assertEquals("", blankEnrichmentResult.get("dobAsDate").getValue(0));
  }
}
