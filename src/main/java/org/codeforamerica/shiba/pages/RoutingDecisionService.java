package org.codeforamerica.shiba.pages;

import static org.codeforamerica.shiba.County.Becker;
import static org.codeforamerica.shiba.County.Clearwater;
import static org.codeforamerica.shiba.County.Mahnomen;
import static org.codeforamerica.shiba.Program.*;
import static org.codeforamerica.shiba.TribalNationRoutingDestination.MILLE_LACS_BAND_OF_OJIBWE;
import static org.codeforamerica.shiba.TribalNationRoutingDestination.WHITE_EARTH;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.APPLYING_FOR_TRIBAL_TANF;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.SELECTED_TRIBAL_NATION;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getFirstValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.CountyMap;
import org.codeforamerica.shiba.TribalNationRoutingDestination;
import org.codeforamerica.shiba.application.parsers.CountyParser;
import org.codeforamerica.shiba.mnit.CountyRoutingDestination;
import org.codeforamerica.shiba.mnit.RoutingDestination;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.springframework.stereotype.Service;

@Service
// The tests for this class live in MnitDocumentConsumerTest
public class RoutingDecisionService {

  private final CountyParser countyParser;
  private final Map<String, TribalNationRoutingDestination> tribalNations;
  private final CountyMap<CountyRoutingDestination> countyRoutingDestinations;

  public RoutingDecisionService(CountyParser countyParser,
      Map<String, TribalNationRoutingDestination> tribalNations,
      CountyMap<CountyRoutingDestination> countyRoutingDestinations) {
    this.countyParser = countyParser;
    this.tribalNations = tribalNations;
    this.countyRoutingDestinations = countyRoutingDestinations;
  }

  // GOAL: this method needs to return white earth
  public List<RoutingDestination> getRoutingDestinations(ApplicationData applicationData,
      Document document) {
    Set<String> programs = applicationData.getApplicantAndHouseholdMemberPrograms();
    County county = countyParser.parse(applicationData);
    List<RoutingDestination> result = new ArrayList<>();

    boolean shouldSendToWhiteEarth = shouldSendToWhiteEarth(applicationData, county);
    if (shouldSendToWhiteEarth) {
      result.add(tribalNations.get(WHITE_EARTH));
    }

    boolean shouldSendToMilleLacs = shouldSendToMilleLacs(applicationData, document, county);
    if (shouldSendToMilleLacs) {
      result.add(tribalNations.get(MILLE_LACS_BAND_OF_OJIBWE));
    }

    if (shouldSendToCounty(programs, applicationData, document, county)) {
      result.add(countyRoutingDestinations.get(county));
    }

    return result;
  }

  private boolean shouldSendToCounty(Set<String> programs,
      ApplicationData applicationData, Document document, County county) {
    boolean shouldSendToWhiteEarth = shouldSendToWhiteEarth(applicationData, county);
    boolean shouldSendToMilleLacs = shouldSendToMilleLacs(applicationData, document, county);
    return !shouldSendToWhiteEarth && (!shouldSendToMilleLacs
        || programs.contains(SNAP) || programs.contains(CASH)
        || programs.contains(GRH) || programs.contains(CCAP));
  }

  private boolean shouldSendToWhiteEarth(ApplicationData applicationData, County county) {
    var pagesData = applicationData.getPagesData();
    var selectedTribeName = getFirstValue(pagesData, SELECTED_TRIBAL_NATION);

    return selectedTribeName != null
        && selectedTribeName.equals(WHITE_EARTH)
        && List.of(Becker, Mahnomen, Clearwater).contains(county);
  }

  // Send to Mille Lacs if the tribe is serviced by Mille Lacs and applying for Tribal TANF and/or EA
  private boolean shouldSendToMilleLacs(ApplicationData applicationData, Document document,
      County county) {
    var pagesData = applicationData.getPagesData();
    var selectedTribeName = getFirstValue(pagesData, SELECTED_TRIBAL_NATION);
    var applyingForTribalTanf = Boolean.parseBoolean(
        getFirstValue(pagesData, APPLYING_FOR_TRIBAL_TANF));
    var programs = applicationData.getApplicantAndHouseholdMemberPrograms();

    return selectedTribeName != null
        && tribalNations.get(selectedTribeName) != null
        && tribalNations.get(selectedTribeName).isServicedByMilleLacs()
        && (applyingForTribalTanf || programs.contains(EA))
        && !Document.CCAP.equals(document)
        && !shouldSendToWhiteEarth(applicationData, county);
  }
}
