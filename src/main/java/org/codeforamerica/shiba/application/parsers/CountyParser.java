package org.codeforamerica.shiba.application.parsers;

import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.ENRICHED_HOME_COUNTY;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.IDENTIFY_COUNTY;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.IDENTIFY_COUNTY_LATER_DOCS;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.IDENTIFY_COUNTY_HEALTHCARE_RENEWAL;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getFirstValue;

import lombok.extern.slf4j.Slf4j;
import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.application.FlowType;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CountyParser {

  public static County parse(ApplicationData applicationData) {
    String countyName = getFirstValue(applicationData.getPagesData(), IDENTIFY_COUNTY);
    String countyNameLD = "Other";
    if (applicationData.getFlow().equals(FlowType.LATER_DOCS)) {
    	countyNameLD = getFirstValue(applicationData.getPagesData(), IDENTIFY_COUNTY_LATER_DOCS);
    }
    if (applicationData.getFlow().equals(FlowType.HEALTHCARE_RENEWAL)) {
    	countyNameLD = getFirstValue(applicationData.getPagesData(), IDENTIFY_COUNTY_HEALTHCARE_RENEWAL);
    }

    try {
      return County.getForName(!countyNameLD.equalsIgnoreCase("Other")?countyNameLD:countyName);
    } catch (Exception e) {
      log.warn("Could not retrieve County object corresponding to county name: " + countyName, e);
      return County.Other;
    }
  }
  
  public static County parseEnrich(ApplicationData applicationData) {
    String countyName = getFirstValue(applicationData.getPagesData(), ENRICHED_HOME_COUNTY);
    try {
      return County.getForName(countyName);
    } catch (Exception e) {
      log.warn("Could not retrieve Enriched County object corresponding to county name: " + countyName, e);
      return County.Other;
    }
  }
}