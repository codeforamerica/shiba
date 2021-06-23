package org.codeforamerica.shiba.pages.enrichment;

import org.codeforamerica.shiba.pages.data.PagesData;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.PERSONAL_INFO_DOB;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getValues;

@Component
public class PersonalInfoDateOfBirthEnrichment extends DateOfBirthEnrichment {
    @Override
    protected List<String> parseDateOfBirth(PagesData pagesData) {
        return getValues(pagesData, PERSONAL_INFO_DOB);
    }
}
