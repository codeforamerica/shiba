package org.codeforamerica.shiba.pages;

import static org.codeforamerica.shiba.Program.*;
import static org.codeforamerica.shiba.TribalNation.*;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.APPLYING_FOR_TRIBAL_TANF;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.IDENTIFY_COUNTY_LATER_DOCS;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.LIVING_IN_TRIBAL_NATION_BOUNDARY;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.SELECTED_TRIBAL_NATION;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.IDENTIFY_TRIBAL_NATION_LATER_DOCS;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getBooleanValue;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getFirstValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.ServicingAgencyMap;
import org.codeforamerica.shiba.TribalNation;
import org.codeforamerica.shiba.TribalNationRoutingDestination;
import org.codeforamerica.shiba.application.FlowType;
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

  private final List<String> TRIBES_WE_CAN_ROUTE_TO = Stream.of(MilleLacsBandOfOjibwe,
      WhiteEarthNation, BoisForte, FondDuLac, GrandPortage, LeechLake, RedLakeNation,
      OtherFederallyRecognizedTribe).map(Enum::toString).toList();
  private final ServicingAgencyMap<TribalNationRoutingDestination> tribalNations;
  private final ServicingAgencyMap<CountyRoutingDestination> countyRoutingDestinations;
  public RoutingDecisionService(ServicingAgencyMap<TribalNationRoutingDestination> tribalNations,
      @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") ServicingAgencyMap<CountyRoutingDestination> countyRoutingDestinations,
      FeatureFlagConfiguration featureFlagConfiguration) {
    this.tribalNations = tribalNations;
    this.countyRoutingDestinations = countyRoutingDestinations;
  }

  public List<RoutingDestination> getRoutingDestinations(ApplicationData applicationData,
      Document document) {
    if (applicationData.getFlow() == FlowType.LATER_DOCS) {
	    return getLaterDocsRoutingDestinations(applicationData, document);
    }
    Set<String> programs = applicationData.getApplicantAndHouseholdMemberPrograms();
    County county = CountyParser.parse(applicationData);
    String tribeName = getFirstValue(applicationData.getPagesData(), SELECTED_TRIBAL_NATION);

    if (tribeName != null && TRIBES_WE_CAN_ROUTE_TO.contains(tribeName)) {
      TribalNation tribalNation = TribalNation.getFromName(tribeName);
      // Route members of Tribal Nations we service
      return switch (tribalNation) {
        case WhiteEarthNation -> routeWhiteEarthClients(programs, applicationData, document, county);
        case MilleLacsBandOfOjibwe, BoisForte, FondDuLac, GrandPortage, LeechLake ->
            routeClientsServicedByMilleLacs(
                programs, applicationData, document, county);
        case RedLakeNation -> routeRedLakeClients(programs, applicationData, county);
        case OtherFederallyRecognizedTribe -> routeClientsInOtherFederallyRecognizedTribe(
            county);
        default -> List.of(countyRoutingDestinations.get(county));
      };
    }

    // By default, just send to county
    return List.of(countyRoutingDestinations.get(county));
  }

  private List<RoutingDestination> getLaterDocsRoutingDestinations(ApplicationData applicationData,
	      Document document) {
	List<RoutingDestination> result = new ArrayList<>();
	String countyName = getFirstValue(applicationData.getPagesData(), IDENTIFY_COUNTY_LATER_DOCS);
	if (countyName != null && !countyName.isEmpty()) {
		County county = County.getForName(countyName);
		RoutingDestination destination = countyRoutingDestinations.get(county);
		result.add(destination);
	}
	String tribalNationName = getFirstValue(applicationData.getPagesData(), IDENTIFY_TRIBAL_NATION_LATER_DOCS);
	if (tribalNationName != null && !tribalNationName.isEmpty()) {
		result.add(tribalNations.get(TribalNation.getFromName(tribalNationName)));
	}
	return result;
  }

  public RoutingDestination getRoutingDestinationByName(String name) {
    RoutingDestination result;
    try {
      result = tribalNations.get(TribalNation.getFromName(name));
    } catch (IllegalArgumentException e) {
      result = countyRoutingDestinations.get(County.getForName(name));
    }
    return result;
  }

  private List<RoutingDestination> routeClientsInOtherFederallyRecognizedTribe(
      County county) {
    if (!county.equals(County.Beltrami)) {
      return List.of(countyRoutingDestinations.get(county));
    }
    return List.of(tribalNations.get(RedLakeNation));
  }

  private List<RoutingDestination> routeRedLakeClients(Set<String> programs,
      ApplicationData applicationData, County county) {

    boolean isLivingInTribalNationBoundary = getBooleanValue(applicationData.getPagesData(),
        LIVING_IN_TRIBAL_NATION_BOUNDARY);
    if (!isLivingInTribalNationBoundary || isOnlyApplyingForGrh(programs, applicationData)) {
      return List.of(countyRoutingDestinations.get(county));
    }

    if (programs.contains(GRH)) {
      return List.of(countyRoutingDestinations.get(county), tribalNations.get(RedLakeNation));
    }

    return List.of(tribalNations.get(RedLakeNation));
  }

  private boolean isOnlyApplyingForGrh(Set<String> programs, ApplicationData applicationData) {
    return programs.size() == 1 && programs.contains(GRH) &&
        !isApplyingForTribalTanf(applicationData.getPagesData());
  }

  private List<RoutingDestination> routeWhiteEarthClients(Set<String> programs,
      ApplicationData applicationData,
      Document document, County county) {

    var pagesData = applicationData.getPagesData();
    var selectedTribeName = getFirstValue(pagesData, SELECTED_TRIBAL_NATION);

    if (livesInCountyServicedByWhiteEarth(county, selectedTribeName)) {
      return List.of(tribalNations.get(WhiteEarthNation));
    }

    if (URBAN_COUNTIES.contains(county)) {
      return routeClientsServicedByMilleLacs(programs, applicationData, document, county);
    }
    return List.of(countyRoutingDestinations.get(county));
  }

  private boolean livesInCountyServicedByWhiteEarth(County county, String selectedTribeName) {
    return selectedTribeName != null
        && selectedTribeName.equals(WhiteEarthNation.toString())
        && COUNTIES_SERVICED_BY_WHITE_EARTH.contains(county);
  }

  private boolean isApplyingForTribalTanf(PagesData pagesData) {
    return getBooleanValue(pagesData, APPLYING_FOR_TRIBAL_TANF);
  }

  private List<RoutingDestination> routeClientsServicedByMilleLacs(Set<String> programs,
      ApplicationData applicationData, Document document, County county) {
    List<RoutingDestination> result = new ArrayList<>();
    if (shouldSendToMilleLacs(applicationData, document)) {
      result.add(tribalNations.get(MilleLacsBandOfOjibwe));
    }
    if (shouldSendToCounty(programs, applicationData, document)) {
      result.add(countyRoutingDestinations.get(county));
    }
    return result;
  }

  private boolean shouldSendToCounty(Set<String> programs, ApplicationData applicationData,
      Document document) {
    boolean shouldSendToMilleLacs = shouldSendToMilleLacs(applicationData, document);
    boolean isApplicableForCcap = programs.contains(CCAP) &&
        (document == Document.CCAP || document == Document.UPLOADED_DOC);
    return !shouldSendToMilleLacs
        || isApplicableForCcap
        || programs.contains(SNAP) || programs.contains(CASH) || programs.contains(GRH);
  }

  private boolean shouldSendToMilleLacs(ApplicationData applicationData, Document document) {
    var pagesData = applicationData.getPagesData();
    var selectedTribeName = getFirstValue(pagesData, SELECTED_TRIBAL_NATION);
    var programs = applicationData.getApplicantAndHouseholdMemberPrograms();

    return selectedTribeName != null
        && tribalNations.get(TribalNation.getFromName(selectedTribeName)) != null
        && (isApplyingForTribalTanf(pagesData) || programs.contains(EA))
        && Document.CCAP != document;
  }
}

