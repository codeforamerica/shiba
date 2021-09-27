package org.codeforamerica.shiba.pages;

import static org.codeforamerica.shiba.Program.CASH;
import static org.codeforamerica.shiba.Program.CCAP;
import static org.codeforamerica.shiba.Program.EA;
import static org.codeforamerica.shiba.Program.GRH;
import static org.codeforamerica.shiba.Program.SNAP;
import static org.codeforamerica.shiba.TribalNation.MILLE_LACS_BAND_OF_OJIBWE;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.APPLYING_FOR_TRIBAL_TANF;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.SELECTED_TRIBAL_NATION;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getFirstValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.codeforamerica.shiba.TribalNation;
import org.codeforamerica.shiba.application.parsers.CountyParser;
import org.codeforamerica.shiba.mnit.RoutingDestination;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.springframework.stereotype.Service;

@Service
// The tests for this class live in MnitDocumentConsumerTest
public class RoutingDecisionService {

  private final CountyParser countyParser;
  private final Map<String, TribalNation> tribalNations;

  public RoutingDecisionService(CountyParser countyParser,
      Map<String, TribalNation> tribalNations) {
    this.countyParser = countyParser;
    this.tribalNations = tribalNations;
  }

  public CountyAndRoutingDestinations getRoutingDestination(ApplicationData applicationData,
      Document document) {
    Set<String> programs = applicationData.getApplicantAndHouseholdMemberPrograms();
    CountyAndRoutingDestinations result = new CountyAndRoutingDestinations();

    boolean shouldSendToMilleLacs = shouldSendToMilleLacs(applicationData, document);
    if (shouldSendToMilleLacs) {
      result.routingDestinations.add(tribalNations.get(MILLE_LACS_BAND_OF_OJIBWE));
    }

    // Send to county for all other programs
    if (!shouldSendToMilleLacs
        || programs.contains(SNAP) || programs.contains(CASH)
        || programs.contains(GRH) || programs.contains(CCAP)) {
      result.setCounty(countyParser.parseCountyInput(applicationData));
    }

    return result;
  }

  // Send to Mille Lacs if the tribe is serviced by Mille Lacs and applying for Tribal TANF and/or EA
  private boolean shouldSendToMilleLacs(ApplicationData applicationData, Document document) {
    var pagesData = applicationData.getPagesData();
    var selectedTribeName = getFirstValue(pagesData, SELECTED_TRIBAL_NATION);
    var applyingForTribalTanf = Boolean.parseBoolean(
        getFirstValue(pagesData, APPLYING_FOR_TRIBAL_TANF));
    var programs = applicationData.getApplicantAndHouseholdMemberPrograms();

    return selectedTribeName != null
        && tribalNations.get(selectedTribeName) != null
        && tribalNations.get(selectedTribeName).isServicedByMilleLacs()
        && (applyingForTribalTanf || programs.contains(EA))
        && !Document.CCAP.equals(document);
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class CountyAndRoutingDestinations { // TODO remove county and then remove this entirely

    private String county;
    //    private String tribalNation;
    private List<RoutingDestination> routingDestinations = new ArrayList<>();
  }
}
