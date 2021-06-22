package org.codeforamerica.shiba.pages.enrichment;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.codeforamerica.shiba.PageDataBuilder;
import org.codeforamerica.shiba.PagesDataBuilder;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.junit.jupiter.api.Test;

public class DateOfBirthEnrichmentTest {
    private final PersonalInfoDateOfBirthEnrichment personalInfoDataOfBirthEnrichment = new PersonalInfoDateOfBirthEnrichment();

    @Test
    void dobAsDateIsPresentInPersonalInfoDateOfBirthEnrichment() {
        ApplicationData applicationData = new ApplicationData();
        PagesData pagesData = new PagesDataBuilder().build(List.of(
                new PageDataBuilder("personalInfo", Map.of("dateOfBirth", List.of("01","09", "1999")))));
        applicationData.setPagesData(pagesData);

        EnrichmentResult enrichmentResult = personalInfoDataOfBirthEnrichment.process(applicationData);
        assertNotNull(enrichmentResult.get("dobAsDate"));
       	assertTrue(enrichmentResult.get("dobAsDate").getValue(0).equals("01/09/1999"));
    }

}
