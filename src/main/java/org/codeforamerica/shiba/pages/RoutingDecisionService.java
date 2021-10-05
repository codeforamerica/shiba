package org.codeforamerica.shiba.pages;

import static org.codeforamerica.shiba.Program.*;
import static org.codeforamerica.shiba.TribalNationRoutingDestination.*;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.APPLYING_FOR_TRIBAL_TANF;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.LIVING_IN_TRIBAL_NATION_BOUNDARY;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.SELECTED_TRIBAL_NATION;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getBooleanValue;
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
import org.codeforamerica.shiba.pages.config.FeatureFlagConfiguration;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.springframework.stereotype.Service;

@SuppressWarnings("DanglingJavadoc")
@Service
/**
 * The tests for this class live in a few places:
 * @see org.codeforamerica.shiba.pages.TribalNationsMockMvcTest
 * @see org.codeforamerica.shiba.output.MnitDocumentConsumerTest
 */
public class RoutingDecisionService {

  private final List<String> TRIBES_WE_CAN_ROUTE_TO = List.of(MILLE_LACS_BAND_OF_OJIBWE,
      WHITE_EARTH, BOIS_FORTE, FOND_DU_LAC, GRAND_PORTAGE, LEECH_LAKE, RED_LAKE,
      OTHER_FEDERALLY_RECOGNIZED_TRIBE);
  private final CountyParser countyParser;
  private final Map<String, TribalNationRoutingDestination> tribalNations;
  private final CountyMap<CountyRoutingDestination> countyRoutingDestinations;
  private final FeatureFlagConfiguration featureFlagConfiguration;
  private final String WHITE_EARTH_AND_RED_LAKE_ROUTING_FLAG_NAME = "white-earth-and-red-lake-routing";

  public RoutingDecisionService(CountyParser countyParser,
      Map<String, TribalNationRoutingDestination> tribalNations,
      CountyMap<CountyRoutingDestination> countyRoutingDestinations,
      FeatureFlagConfiguration featureFlagConfiguration) {
    this.countyParser = countyParser;
    this.tribalNations = tribalNations;
    this.countyRoutingDestinations = countyRoutingDestinations;
    this.featureFlagConfiguration = featureFlagConfiguration;
  }

  public List<RoutingDestination> getRoutingDestinations(ApplicationData applicationData,
      Document document) {
    Set<String> programs = applicationData.getApplicantAndHouseholdMemberPrograms();
    County county = countyParser.parse(applicationData);
    String tribeName = getFirstValue(applicationData.getPagesData(), SELECTED_TRIBAL_NATION);

    if (tribeName != null && TRIBES_WE_CAN_ROUTE_TO.contains(tribeName)) {
      // Route members of Tribal Nations we service
      return switch (tribeName) {
        case WHITE_EARTH -> routeWhiteEarthClients(programs, applicationData, document, county);
        case MILLE_LACS_BAND_OF_OJIBWE, BOIS_FORTE, FOND_DU_LAC, GRAND_PORTAGE, LEECH_LAKE -> routeClientsServicedByMilleLacs(
            programs, applicationData, document, county);
        case RED_LAKE -> routeRedLakeClients(programs, applicationData, county);
        case OTHER_FEDERALLY_RECOGNIZED_TRIBE -> routeClientsInOtherFederallyRecognizedTribe(
            programs, applicationData, document, county);
        default -> List.of(countyRoutingDestinations.get(county));
      };
    }

    // By default, just send to county
    return List.of(countyRoutingDestinations.get(county));
  }

  private List<RoutingDestination> routeClientsInOtherFederallyRecognizedTribe(
      Set<String> programs, ApplicationData applicationData, Document document, County county) {
    if (county.equals(County.Beltrami)) {
      return List.of(tribalNations.get(RED_LAKE));
    }
    return List.of(countyRoutingDestinations.get(county));
  }

  private List<RoutingDestination> routeRedLakeClients(Set<String> programs,
      ApplicationData applicationData, County county) {

    boolean isLivingInTribalNationBoundary = getBooleanValue(applicationData.getPagesData(),
        LIVING_IN_TRIBAL_NATION_BOUNDARY);
    if (!isLivingInTribalNationBoundary || isOnlyApplyingForGrh(programs, applicationData)
        || featureFlagConfiguration.get(WHITE_EARTH_AND_RED_LAKE_ROUTING_FLAG_NAME).isOff()) {
      return List.of(countyRoutingDestinations.get(county));
    }

    if (programs.contains(GRH)) {
      return List.of(countyRoutingDestinations.get(county), tribalNations.get(RED_LAKE));
    }

    return List.of(tribalNations.get(RED_LAKE));
  }

  private boolean isOnlyApplyingForGrh(Set<String> programs, ApplicationData applicationData) {
    return programs.size() == 1 && programs.contains(GRH) &&
        !isApplyingForTribalTanf(applicationData.getPagesData());
  }

  private List<RoutingDestination> routeWhiteEarthClients(Set<String> programs,
      ApplicationData applicationData,
      Document document, County county) {
    if (featureFlagConfiguration.get(WHITE_EARTH_AND_RED_LAKE_ROUTING_FLAG_NAME).isOff()) {
      return List.of(countyRoutingDestinations.get(county));
    }

    var pagesData = applicationData.getPagesData();
    var selectedTribeName = getFirstValue(pagesData, SELECTED_TRIBAL_NATION);

    if (livesInCountyServicedByWhiteEarth(county, selectedTribeName)) {
      return List.of(tribalNations.get(WHITE_EARTH));
    }

    if (URBAN_COUNTIES.contains(county)) {
      return routeClientsServicedByMilleLacs(programs, applicationData, document, county);
    }
    return List.of(countyRoutingDestinations.get(county));
  }

  private boolean livesInCountyServicedByWhiteEarth(County county, String selectedTribeName) {
    return selectedTribeName != null
        && selectedTribeName.equals(WHITE_EARTH)
        && COUNTIES_SERVICED_BY_WHITE_EARTH.contains(county);
  }

  private boolean isApplyingForTribalTanf(PagesData pagesData) {
    return getBooleanValue(pagesData, APPLYING_FOR_TRIBAL_TANF);
  }

  private List<RoutingDestination> routeClientsServicedByMilleLacs(Set<String> programs,
      ApplicationData applicationData, Document document, County county) {
    List<RoutingDestination> result = new ArrayList<>();
    if (shouldSendToMilleLacs(applicationData, document)) {
      result.add(tribalNations.get(MILLE_LACS_BAND_OF_OJIBWE));
    }
    if (shouldSendToCounty(programs, applicationData, document)) {
      result.add(countyRoutingDestinations.get(county));
    }
    return result;
  }

  private boolean shouldSendToCounty(Set<String> programs, ApplicationData applicationData,
      Document document) {
    boolean shouldSendToMilleLacs = shouldSendToMilleLacs(applicationData, document);
    return !shouldSendToMilleLacs
        || programs.contains(SNAP) || programs.contains(CASH)
        || programs.contains(GRH) || programs.contains(CCAP);
  }

  private boolean shouldSendToMilleLacs(ApplicationData applicationData, Document document) {
    var pagesData = applicationData.getPagesData();
    var selectedTribeName = getFirstValue(pagesData, SELECTED_TRIBAL_NATION);
    var programs = applicationData.getApplicantAndHouseholdMemberPrograms();

    return selectedTribeName != null
        && tribalNations.get(selectedTribeName) != null
        && (isApplyingForTribalTanf(pagesData) || programs.contains(EA))
        && !Document.CCAP.equals(document);
  }
}

