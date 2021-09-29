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
    Set<String> programs = applicationData.getApplicantAndHouseholdMemberPrograms(); // ["CCAP", "SNAP", "EA"]
    County county = countyParser.parse(applicationData);
    List<RoutingDestination> result = new ArrayList<>(); // []

    if (shouldSendToWhiteEarth(applicationData, document, county)) {
      result.add(tribalNations.get(WHITE_EARTH));
      return result;
    }

    boolean shouldSendToMilleLacs = shouldSendToMilleLacs(applicationData,
        document); // need to change this
    if (shouldSendToMilleLacs) {
      result.add(tribalNations.get(MILLE_LACS_BAND_OF_OJIBWE)); // ["Mille Lacs Band of Ojibwe"]
    }

    // Send to county for all other programs
    if (!shouldSendToMilleLacs
        || programs.contains(SNAP) || programs.contains(CASH)
        || programs.contains(GRH) || programs.contains(CCAP)) { // need to change this

      result.add(countyRoutingDestinations.get(county));
    }

    return result; // getting this: ["Mille Lacs Band of Ojibwe", "Becker"]
    // we want this: ["White Earth"]
  }

  private boolean shouldSendToWhiteEarth(ApplicationData applicationData, Document document,
      County county) {
    // What tribe are they in?
    var pagesData = applicationData.getPagesData();
    var selectedTribeName = getFirstValue(pagesData, SELECTED_TRIBAL_NATION);

    return selectedTribeName != null
        && selectedTribeName.equals(WHITE_EARTH)
        && List.of(Becker, Mahnomen, Clearwater).contains(county);
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
}
