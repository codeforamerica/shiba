package org.codeforamerica.shiba.pages.enrichment;

import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.MATCH_INFO_DOB;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getValues;

import java.util.List;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.springframework.stereotype.Component;

@Component
public class MatchInfoDateOfBirthEnrichment extends DateOfBirthEnrichment {

  @Override
  protected List<String> parseDateOfBirth(PagesData pagesData) {
    return getValues(pagesData, MATCH_INFO_DOB);
  }
}
