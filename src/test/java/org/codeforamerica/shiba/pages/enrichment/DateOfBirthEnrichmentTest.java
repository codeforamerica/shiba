package org.codeforamerica.shiba.pages.enrichment;

import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.codeforamerica.shiba.pages.data.TestApplicationDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DateOfBirthEnrichmentTest {
    private ApplicationData applicationData;

    @BeforeEach
    void setup() {
        applicationData = new TestApplicationDataBuilder()
                .withPageData("personalInfo", "dateOfBirth", List.of("01","09", "1999"))
                .withPageData("matchInfo", "dateOfBirth", List.of("02","10", "1999"))
                .withHouseholdMember()
                .build();
    }

    @Test
    void dobAsDateIsPresentInPersonalInfoDateOfBirthEnrichment() {
        DateOfBirthEnrichment personalInfoDataOfBirthEnrichment = new PersonalInfoDateOfBirthEnrichment();
        EnrichmentResult enrichmentResult = personalInfoDataOfBirthEnrichment.process(applicationData.getPagesData());

        assertNotNull(enrichmentResult.get("dobAsDate"));
        assertEquals("01/09/1999", enrichmentResult.get("dobAsDate").getValue(0));
    }

    @Test
    void dobAsDateIsPresentInMatchInfoDateOfBirthEnrichment() {
        DateOfBirthEnrichment enrichment = new MatchInfoDateOfBirthEnrichment();

        EnrichmentResult enrichmentResult = enrichment.process(applicationData.getPagesData());

        assertNotNull(enrichmentResult.get("dobAsDate"));
        assertEquals("02/10/1999", enrichmentResult.get("dobAsDate").getValue(0));
    }

    @Test
    void dobAsDateIsPresentInHouseholdMemberInfoDateOfBirthEnrichment() {
        DateOfBirthEnrichment enrichment = new HouseholdMemberDateOfBirthEnrichment();

        PagesData householdPagesData = applicationData.getSubworkflows().get("household").get(0).getPagesData();
        EnrichmentResult enrichmentResult = enrichment.process(householdPagesData);

        assertNotNull(enrichmentResult.get("dobAsDate"));
        assertEquals("05/06/1978", enrichmentResult.get("dobAsDate").getValue(0));
    }

}
