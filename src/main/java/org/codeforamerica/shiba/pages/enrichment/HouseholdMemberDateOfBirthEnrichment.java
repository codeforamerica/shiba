package org.codeforamerica.shiba.pages.enrichment;

import org.codeforamerica.shiba.pages.data.PagesData;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.HOUSEHOLD_INFO_DOB;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getValues;

@Component
public class HouseholdMemberDateOfBirthEnrichment extends DateOfBirthEnrichment {
    @Override
    protected List<String> parseDateOfBirth(PagesData pagesData) {
        return getValues(pagesData, HOUSEHOLD_INFO_DOB);
    }
}
