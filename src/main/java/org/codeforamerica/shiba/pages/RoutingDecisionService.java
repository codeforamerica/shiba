package org.codeforamerica.shiba.pages;

import static org.codeforamerica.shiba.County.Becker;
import static org.codeforamerica.shiba.County.Clearwater;
import static org.codeforamerica.shiba.County.Mahnomen;
import static org.codeforamerica.shiba.Program.*;
import static org.codeforamerica.shiba.TribalNationRoutingDestination.*;
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
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

@Service
// The tests for this class live in MnitDocumentConsumerTest
public class RoutingDecisionService {

  private final List<String> TRIBES_WE_SERVICE = List.of(MILLE_LACS_BAND_OF_OJIBWE, WHITE_EARTH,
      BOIS_FORTE, FOND_DU_LAC, GRAND_PORTAGE, LEECH_LAKE);
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

  public List<RoutingDestination> getRoutingDestinations(ApplicationData applicationData,
      Document document) {
    Set<String> programs = applicationData.getApplicantAndHouseholdMemberPrograms();
    County county = countyParser.parse(applicationData);

    String tribeName = getFirstValue(applicationData.getPagesData(), SELECTED_TRIBAL_NATION);
    if (tribeName != null && TRIBES_WE_SERVICE.contains(tribeName)) {
      // Route members of Tribal Nations we service
      return switch (tribeName) {
        case WHITE_EARTH -> getDestinationsForWhiteEarth(applicationData, document, programs,
            county);
        case MILLE_LACS_BAND_OF_OJIBWE, BOIS_FORTE, FOND_DU_LAC, GRAND_PORTAGE, LEECH_LAKE -> getDestinationsForMilleLacs(
            programs, applicationData, document, county);
        default -> List.of(countyRoutingDestinations.get(county)); // not a situation now??
      };
    }

    // By default, just send to county
    return List.of(countyRoutingDestinations.get(county));
  }

  @NotNull
  private List<RoutingDestination> getDestinationsForWhiteEarth(ApplicationData applicationData,
      Document document, Set<String> programs, County county) {
    boolean shouldSendToWhiteEarth = shouldSendToWhiteEarth(applicationData, county);
    if (shouldSendToWhiteEarth) {
      return List.of(tribalNations.get(WHITE_EARTH));
    } else {
      return List.of(countyRoutingDestinations.get(county));
    }
  }

  private List<RoutingDestination> getDestinationsForMilleLacs(Set<String> programs,
      ApplicationData applicationData, Document document, County county) {
    List<RoutingDestination> result = new ArrayList<>();
    if (shouldSendToMilleLacs(applicationData, document, county)) {
      result.add(tribalNations.get(MILLE_LACS_BAND_OF_OJIBWE));
    }
    if (shouldSendToCounty(programs, applicationData, document, county)) {
      result.add(countyRoutingDestinations.get(county));
    }
    return result;
  }

  private boolean shouldSendToCounty(Set<String> programs,
      ApplicationData applicationData, Document document, County county) {
    boolean shouldSendToMilleLacs = shouldSendToMilleLacs(applicationData, document, county);
    return !shouldSendToMilleLacs
        || programs.contains(SNAP) || programs.contains(CASH)
        || programs.contains(GRH) || programs.contains(CCAP);
  }

  // TODO do we need this method?
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
