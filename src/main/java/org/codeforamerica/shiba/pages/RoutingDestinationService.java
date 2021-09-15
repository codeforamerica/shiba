package org.codeforamerica.shiba.pages;

import static org.codeforamerica.shiba.Program.CASH;
import static org.codeforamerica.shiba.Program.CCAP;
import static org.codeforamerica.shiba.Program.EA;
import static org.codeforamerica.shiba.Program.GRH;
import static org.codeforamerica.shiba.Program.SNAP;
import static org.codeforamerica.shiba.TribalNation.MILLE_LACS;
import static org.codeforamerica.shiba.TribalNation.isServicedByMilleLacs;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.APPLYING_FOR_TRIBAL_TANF;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.SELECTED_TRIBAL_NATION;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getFirstValue;

import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.codeforamerica.shiba.application.parsers.CountyParser;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.springframework.stereotype.Service;

@Service
public class RoutingDestinationService {

  private final CountyParser countyParser;

  // TODO test this
  public RoutingDestinationService(CountyParser countyParser) {
    this.countyParser = countyParser;
  }

  public RoutingDestination getRoutingDestination(ApplicationData applicationData,
      Document document) {
    PagesData pagesData = applicationData.getPagesData();
    String selectedTribe = getFirstValue(pagesData, SELECTED_TRIBAL_NATION);
    Set<String> programs = applicationData.getApplicantAndHouseholdMemberPrograms();
    boolean applyingForTribalTanf = Boolean.parseBoolean(
        getFirstValue(pagesData, APPLYING_FOR_TRIBAL_TANF));

    RoutingDestination result = new RoutingDestination();

    // Send to Mille Lacs if the tribe is serviced by Mille Lacs and applying for Tribal TANF and/or EA
    if (isServicedByMilleLacs(selectedTribe) && (applyingForTribalTanf || programs.contains(EA))) {
      //TODO handle feature flag in here
      result.setTribalNation(MILLE_LACS);
    }

    // Send to county for all other programs
    if (programs.contains(SNAP) || programs.contains(CASH) || programs.contains(GRH)
        || programs.contains(CCAP)) {
      result.setCounty(countyParser.parseCountyInput(applicationData));
    }

    return result;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class RoutingDestination {

    //TODO use the type system instead of strings here
    private String county;
    private String tribalNation;
  }
}
