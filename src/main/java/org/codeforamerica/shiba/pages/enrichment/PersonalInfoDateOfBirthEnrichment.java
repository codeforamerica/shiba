package org.codeforamerica.shiba.pages.enrichment;

import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getValues;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.PERSONAL_INFO_DOB;

import java.util.List;

import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.springframework.stereotype.Component;

@Component
public class PersonalInfoDateOfBirthEnrichment extends DateOfBirthEnrichment {
    @Override
    protected List<String> parseDateOfBirth(ApplicationData applicationData) {
    	return getValues(applicationData.getPagesData(), PERSONAL_INFO_DOB);
    }

}
