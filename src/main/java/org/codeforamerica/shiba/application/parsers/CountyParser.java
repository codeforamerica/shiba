package org.codeforamerica.shiba.application.parsers;

import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.IDENTIFY_COUNTY;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getFirstValue;

import lombok.extern.slf4j.Slf4j;
import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CountyParser {

  public static County parse(ApplicationData applicationData) {
    String countyName = getFirstValue(applicationData.getPagesData(), IDENTIFY_COUNTY);

    try {
      return County.getCountyForName(countyName);
    } catch (Exception e) {
      log.error("Could not retrieve County object corresponding to county name: " + countyName, e);
      return County.Other;
    }
  }
}