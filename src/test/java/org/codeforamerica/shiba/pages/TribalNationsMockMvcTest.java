package org.codeforamerica.shiba.pages;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.County.Beltrami;
import static org.codeforamerica.shiba.Program.*;
import static org.codeforamerica.shiba.TribalNationRoutingDestination.OTHER_FEDERALLY_RECOGNIZED_TRIBE;
import static org.codeforamerica.shiba.TribalNationRoutingDestination.RED_LAKE;
import static org.codeforamerica.shiba.TribalNationRoutingDestination.WHITE_EARTH;
import static org.codeforamerica.shiba.output.Document.CAF;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.CountyMap;
import org.codeforamerica.shiba.application.parsers.CountyParser;
import org.codeforamerica.shiba.mnit.CountyRoutingDestination;
import org.codeforamerica.shiba.mnit.RoutingDestination;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.pages.config.FeatureFlag;
import org.codeforamerica.shiba.pages.enrichment.Address;
import org.codeforamerica.shiba.testutilities.AbstractShibaMockMvcTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

public class TribalNationsMockMvcTest extends AbstractShibaMockMvcTest {

  @Autowired
  private RoutingDecisionService routingDecisionService;
  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  @Autowired
  private CountyMap<CountyRoutingDestination> countyMap;
  @MockBean
  private CountyParser countyParser;

  @BeforeEach
  protected void setUp() throws Exception {
    super.setUp();
    when(countyParser.parse(any())).thenReturn(County.Hennepin);
    when(countyParser.parseCountyInput(any())).thenReturn(County.Hennepin.name());
    mockMvc.perform(get("/pages/identifyCountyBeforeApplying").session(session)); // start timer
    postExpectingSuccess("languagePreferences",
        Map.of("writtenLanguage", List.of("ENGLISH"), "spokenLanguage", List.of("ENGLISH"))
    );

    when(featureFlagConfiguration.get("white-earth-and-red-lake-routing")).thenReturn(
        FeatureFlag.ON);
  }

  @ParameterizedTest
  @CsvSource(value = {
      "Lower Sioux,Otter Tail",
      "Prairie Island,Otter Tail",
      "Shakopee Mdewakanton,Otter Tail",
      "Upper Sioux,Otter Tail"
  })
  void tribesThatSeeMfipAndMustLiveInNationBoundaries(String nationName, String county)
      throws Exception {
    addHouseholdMembersWithEA();
    getToPersonalInfoScreen(EA);
    addAddressInGivenCounty(county);

    postExpectingRedirect("tribalNationMember",
        "isTribalNationMember",
        "true",
        "selectTheTribe");
    postExpectingRedirect("selectTheTribe", "selectedTribe", nationName, "nationsBoundary");
    postExpectingRedirect("nationsBoundary",
        "livingInNationBoundary",
        "true",
        "applyForMFIP");

    assertThat(routingDecisionService.getRoutingDestinations(applicationData, CAF))
        .containsExactly(countyMap.get(County.valueFor(county)));
  }

  @ParameterizedTest
  @CsvSource(value = {
      "Bois Forte,Hennepin",
      "Fond Du Lac,Hennepin",
      "Grand Portage,Hennepin",
      "Leech Lake,Hennepin",
      "Mille Lacs Band of Ojibwe,Hennepin",
      "White Earth,Hennepin",
      "Bois Forte,Anoka",
      "Fond Du Lac,Anoka",
      "Grand Portage,Anoka",
      "Leech Lake,Anoka",
      "Mille Lacs Band of Ojibwe,Anoka",
      "White Earth,Anoka",
      "Bois Forte,Ramsey",
      "Fond Du Lac,Ramsey",
      "Grand Portage,Ramsey",
      "Leech Lake,Ramsey",
      "Mille Lacs Band of Ojibwe,Ramsey",
      "White Earth,Ramsey",
      "Mille Lacs Band of Ojibwe,Ramsey",
      "Mille Lacs Band of Ojibwe,Aitkin",
      "Mille Lacs Band of Ojibwe,Benton",
      "Mille Lacs Band of Ojibwe,Crow Wing",
      "Mille Lacs Band of Ojibwe,Morrison",
      "Mille Lacs Band of Ojibwe,Mille Lacs",
      "Mille Lacs Band of Ojibwe,Pine"
  })
  void shouldSkipNationBoundariesPageAddTribalTanfAndRouteToBothMilleLacsAndCounty(
      String nationName, String county)
      throws Exception {
    addHouseholdMembersWithEA();
    goThroughShortTribalTanfFlow(nationName, county, "true", EA, CCAP, GRH, SNAP);
    assertRoutingDestinationIsCorrectForDocument(Document.CAF, "Mille Lacs Band of Ojibwe", county);
    assertRoutingDestinationIsCorrectForDocument(Document.UPLOADED_DOC, "Mille Lacs Band of Ojibwe",
        county);
    assertRoutingDestinationIsCorrectForDocument(Document.CCAP, county);
  }

  @ParameterizedTest
  @CsvSource(value = {"Becker", "Mahnomen", "Clearwater"})
  void routeWhiteEarthApplicationsToWhiteEarthOnlyAndSeeMFIP(String county) throws Exception {
    addHouseholdMembersWithEA();
    goThroughShortMfipFlow(county, WHITE_EARTH, new String[]{EA, CCAP, GRH, SNAP});

    assertRoutingDestinationIsCorrectForDocument(Document.CAF, WHITE_EARTH);
    assertRoutingDestinationIsCorrectForDocument(Document.CCAP, WHITE_EARTH);
    assertRoutingDestinationIsCorrectForDocument(Document.UPLOADED_DOC, WHITE_EARTH);

    var routingDestinations = routingDecisionService.getRoutingDestinations(applicationData, CAF);
    RoutingDestination routingDestination = routingDestinations.get(0);
    assertThat(routingDestination.getFolderId()).isEqualTo("3b0aa880-db45-483d-fa0-7987c9b0c02d");
    assertThat(routingDestination.getDhsProviderId()).isEqualTo("A086642300");
    assertThat(routingDestination.getEmail()).isEqualTo("help+dev@mnbenefits.org");
    assertThat(routingDestination.getPhoneNumber()).isEqualTo("218-935-2359");
  }

  @ParameterizedTest
  @ValueSource(strings = {"Nobles", "Scott", "Meeker"})
  void routeWhiteEarthApplicationsToCountyOnlyAndSeeMfip(String county) throws Exception {
    addHouseholdMembersWithEA();
    goThroughShortMfipFlow(county, WHITE_EARTH, new String[]{EA, CCAP, GRH, SNAP});

    assertRoutingDestinationIsCorrectForDocument(Document.CAF, county);
    assertRoutingDestinationIsCorrectForDocument(Document.CCAP, county);
    assertRoutingDestinationIsCorrectForDocument(Document.UPLOADED_DOC, county);
  }

  @ParameterizedTest
  @CsvSource(value = {
      "Hennepin,true,Mille Lacs Band of Ojibwe",
      "Ramsey,true,Mille Lacs Band of Ojibwe",
      "Anoka,true,Mille Lacs Band of Ojibwe",
      "Hennepin,false,Mille Lacs Band of Ojibwe",
      "Ramsey,false,Mille Lacs Band of Ojibwe",
      "Anoka,false,Mille Lacs Band of Ojibwe",
  })
  void routeUrbanWhiteEarthApplicationsForOnlyEaAndTribalTanf(String county,
      String applyForTribalTANF,
      String destinationName) throws Exception {
    addHouseholdMembersWithEA();

    goThroughShortTribalTanfFlow(WHITE_EARTH, county, applyForTribalTANF, EA);

    assertRoutingDestinationIsCorrectForDocument(Document.CAF, destinationName);
    assertRoutingDestinationIsCorrectForDocument(Document.UPLOADED_DOC, destinationName);
  }

  @ParameterizedTest
  @CsvSource(value = {
      "Bois Forte,Olmsted,Mille Lacs Band of Ojibwe",
      "Fond Du Lac,Olmsted,Mille Lacs Band of Ojibwe",
      "Grand Portage,Olmsted,Mille Lacs Band of Ojibwe",
      "Leech Lake,Olmsted,Mille Lacs Band of Ojibwe",
      "Mille Lacs Band of Ojibwe,Olmsted,Mille Lacs Band of Ojibwe",
      "White Earth,Olmsted,Olmsted",
      "Bois Forte,Aitkin,Mille Lacs Band of Ojibwe",
      "Fond Du Lac,Benton,Mille Lacs Band of Ojibwe",
      "Grand Portage,Crow Wing,Mille Lacs Band of Ojibwe",
      "Leech Lake,Morrison,Mille Lacs Band of Ojibwe",
      "White Earth,Mille Lacs,Mille Lacs",
      "Bois Forte,Pine,Mille Lacs Band of Ojibwe",
      "Federally recognized tribe outside of MN,Otter Tail,Otter Tail"
  })
  void shouldSkipNationBoundariesPageAndSendToMfipScreen(String nationName, String county,
      String expectedRoutingDestination)
      throws Exception {
    addHouseholdMembersWithEA();
    addAddressInGivenCounty(county);

    postExpectingRedirect("tribalNationMember", "isTribalNationMember", "true", "selectTheTribe");
    postExpectingRedirect("selectTheTribe", "selectedTribe", nationName, "applyForMFIP");

    assertRoutingDestinationIsCorrectForDocument(Document.CAF, expectedRoutingDestination);
    assertRoutingDestinationIsCorrectForDocument(Document.UPLOADED_DOC, expectedRoutingDestination);
  }

  @Test
  void clientsFromOtherFederallyRecognizedNationsShouldBeAbleToApplyForTribalTanfAndRouteToRedLake()
      throws Exception {
    // TODO feature flag
    addHouseholdMembersWithEA();
    goThroughShortTribalTanfFlow(OTHER_FEDERALLY_RECOGNIZED_TRIBE, Beltrami.displayName(), "true",
        EA);

    assertRoutingDestinationIsCorrectForDocument(Document.CAF, RED_LAKE);
    assertRoutingDestinationIsCorrectForDocument(Document.UPLOADED_DOC, RED_LAKE);
  }

  @ParameterizedTest
  @CsvSource(value = {
      "Red Lake,Hennepin",
      "Shakopee Mdewakanton,Hennepin"
  })
  void shouldGetBootedFromTheFlowAndSentToCountyIfLivingOutsideOfNationBoundary(String nationName,
      String county)
      throws Exception {
    addHouseholdMembersWithEA();

    getToPersonalInfoScreen(CCAP, SNAP, CASH, EA);

    addAddressInGivenCounty("Hennepin");
    postExpectingRedirect("tribalNationMember",
        "isTribalNationMember",
        "true",
        "selectTheTribe");
    postExpectingRedirect("selectTheTribe", "selectedTribe", nationName, "nationsBoundary");
    postExpectingRedirect("nationsBoundary",
        "livingInNationBoundary",
        "false",
        "introIncome");

    assertRoutingDestinationIsCorrectForDocument(Document.CAF, county);
    assertRoutingDestinationIsCorrectForDocument(Document.UPLOADED_DOC, county);
    assertRoutingDestinationIsCorrectForDocument(Document.CCAP, county);
  }

  @ParameterizedTest
  @CsvSource(value = {
      "Prairie Island,Hennepin,true",
      "Shakopee Mdewakanton,Hennepin,true",
      "Lower Sioux,Ramsey,true",
      "Upper Sioux,Ramsey,true"
  })
  void tribesThatCanApplyForMfipIfWithinNationBoundaries(String nationName, String county,
      String livingInNationBoundary) throws Exception {
    addHouseholdMembersWithEA();

    postExpectingSuccess("identifyCountyBeforeApplying", "county", county);
    postExpectingRedirect("tribalNationMember",
        "isTribalNationMember",
        "true",
        "selectTheTribe");
    postExpectingRedirect("selectTheTribe", "selectedTribe", nationName, "nationsBoundary");
    postExpectingRedirect("nationsBoundary",
        "livingInNationBoundary",
        livingInNationBoundary,
        "applyForMFIP");
  }

  @Test
  void redLakeApplicationsWithoutGrhGetSentToRedLake() throws Exception {
    addHouseholdMembersWithEA();
    goThroughLongTribalTanfFlow(RED_LAKE, "Hennepin", "true", CCAP, SNAP, CASH, EA);

    assertRoutingDestinationIsCorrectForDocument(Document.CAF, RED_LAKE);
    assertRoutingDestinationIsCorrectForDocument(Document.UPLOADED_DOC, RED_LAKE);
    assertRoutingDestinationIsCorrectForDocument(Document.CCAP, RED_LAKE);
  }

  @Test
  void redLakeApplicationsWithOnlySnapGetSentToRedLake() throws Exception {
    addHouseholdMembersWithProgram(SNAP);
    goThroughLongTribalTanfFlow(RED_LAKE, "Hennepin", "false", SNAP);

    assertRoutingDestinationIsCorrectForDocument(Document.CAF, RED_LAKE);
    assertRoutingDestinationIsCorrectForDocument(Document.UPLOADED_DOC, RED_LAKE);
    assertRoutingDestinationIsCorrectForDocument(Document.CCAP, RED_LAKE);
  }

  @Test
  void redLakeApplicationsWithGrhAndTribalTanfGetSentToRedLakeAndCounty() throws Exception {
    addHouseholdMembersWithProgram("GRH");

    String county = "Olmsted";
    goThroughLongTribalTanfFlow(RED_LAKE, county, "true", GRH);

    assertRoutingDestinationIsCorrectForDocument(Document.CAF, county, RED_LAKE);
    assertRoutingDestinationIsCorrectForDocument(Document.UPLOADED_DOC, county, RED_LAKE);
    assertRoutingDestinationIsCorrectForDocument(Document.CCAP, county, RED_LAKE);
  }

  @Test
  void redLakeApplicationsWithGrhAndSnapAndTribalTanfGetSentToRedLakeAndCounty() throws Exception {
    addHouseholdMembersWithProgram(SNAP);

    String county = "Olmsted";
    goThroughLongTribalTanfFlow(RED_LAKE, county, "true", GRH);

    assertRoutingDestinationIsCorrectForDocument(Document.CAF, county, RED_LAKE);
    assertRoutingDestinationIsCorrectForDocument(Document.UPLOADED_DOC, county, RED_LAKE);
    assertRoutingDestinationIsCorrectForDocument(Document.CCAP, county, RED_LAKE);
  }

  @Test
  void redLakeApplicationsWithOnlyGrhAndCcapGetSentToRedLakeAndCounty() throws Exception {
    addHouseholdMembersWithProgram(CCAP);

    String county = "Olmsted";
    goThroughLongTribalTanfFlow(RED_LAKE, county, "false", GRH);

    assertRoutingDestinationIsCorrectForDocument(Document.CAF, county, RED_LAKE);
    assertRoutingDestinationIsCorrectForDocument(Document.UPLOADED_DOC, county, RED_LAKE);
    assertRoutingDestinationIsCorrectForDocument(Document.CCAP, county, RED_LAKE);
  }

  @Test
  void redLakeApplicationsWithGrhOnlyGetSentToCounty() throws Exception {
    addHouseholdMembersWithProgram("GRH");
    getToPersonalInfoScreen(GRH);
    String county = "Anoka";
    addAddressInGivenCounty(county);
    postExpectingRedirect("tribalNationMember",
        "isTribalNationMember",
        "true",
        "selectTheTribe");
    postExpectingRedirect("selectTheTribe", "selectedTribe", RED_LAKE, "nationsBoundary");
    postExpectingRedirect("nationsBoundary",
        "livingInNationBoundary",
        "false",
        "introIncome");

    assertRoutingDestinationIsCorrectForDocument(Document.CAF, county);
    assertRoutingDestinationIsCorrectForDocument(Document.UPLOADED_DOC, county);
    assertRoutingDestinationIsCorrectForDocument(Document.CCAP, county);
  }

  @Test
  void redLakeApplicationsGetSentToCountyIfFeatureFlagIsTurnedOff() throws Exception {
    when(featureFlagConfiguration.get("white-earth-and-red-lake-routing")).thenReturn(
        FeatureFlag.OFF);

    addHouseholdMembersWithProgram(CCAP);

    String county = "Olmsted";
    goThroughLongTribalTanfFlow(RED_LAKE, county, "false", GRH);

    assertRoutingDestinationIsCorrectForDocument(Document.CAF, county);
    assertRoutingDestinationIsCorrectForDocument(Document.UPLOADED_DOC, county);
    assertRoutingDestinationIsCorrectForDocument(Document.CCAP, county);
  }

  @Test
  void whiteEarthApplicationsGetSentToCountyIfFeatureFlagIsTurnedOff() throws Exception {
    when(featureFlagConfiguration.get("white-earth-and-red-lake-routing")).thenReturn(
        FeatureFlag.OFF);

    addHouseholdMembersWithEA();
    String county = "Becker";
    goThroughShortMfipFlow(county, WHITE_EARTH, new String[]{EA, CCAP, GRH, SNAP});

    assertRoutingDestinationIsCorrectForDocument(Document.CAF, county);
    assertRoutingDestinationIsCorrectForDocument(Document.CCAP, county);
    assertRoutingDestinationIsCorrectForDocument(Document.UPLOADED_DOC, county);
  }

  private void goThroughLongTribalTanfFlow(String nationName, String county,
      String applyForTribalTanf,
      String... programs) throws Exception {
    getToPersonalInfoScreen(programs);
    addAddressInGivenCounty(county);

    postExpectingRedirect("tribalNationMember",
        "isTribalNationMember",
        "true",
        "selectTheTribe");
    postExpectingRedirect("selectTheTribe", "selectedTribe", nationName, "nationsBoundary");
    postExpectingRedirect("nationsBoundary",
        "livingInNationBoundary",
        "true",
        "applyForTribalTANF");
    postExpectingRedirect("applyForTribalTANF",
        "applyForTribalTANF",
        applyForTribalTanf,
        applyForTribalTanf.equals("true") ? "tribalTANFConfirmation" : "introIncome");
  }

  private void goThroughShortTribalTanfFlow(String nationName, String county,
      String applyForTribalTanf,
      String... programs) throws Exception {
    getToPersonalInfoScreen(programs);
    addAddressInGivenCounty(county);

    postExpectingRedirect("tribalNationMember",
        "isTribalNationMember",
        "true",
        "selectTheTribe");
    postExpectingRedirect("selectTheTribe",
        "selectedTribe",
        nationName,
        "applyForTribalTANF");
    postExpectingRedirect("applyForTribalTANF",
        "applyForTribalTANF",
        applyForTribalTanf,
        applyForTribalTanf.equals("true") ? "tribalTANFConfirmation" : "introIncome");
  }

  private void assertRoutingDestinationIsCorrectForDocument(Document doc,
      String... expectedNames) {
    List<RoutingDestination> routingDestinations = routingDecisionService.getRoutingDestinations(
        applicationData, doc);
    List<String> routingDestinationNames = routingDestinations.stream()
        .map(RoutingDestination::getName)
        .collect(Collectors.toList());
    assertThat(routingDestinationNames).containsExactly(expectedNames);
  }

  private void addAddressInGivenCounty(String county) throws Exception {
    String countyNameWithoutSpaces = county.replace(" ", "");
    County value = County.valueOf(countyNameWithoutSpaces);
    when(countyParser.parse(any())).thenReturn(value);
    when(countyParser.parseCountyInput(any())).thenReturn(county);
    fillOutPersonalInfo();
    fillOutContactInfo();

    when(locationClient.validateAddress(any())).thenReturn(
        Optional.of(
            new Address("testStreet", "testCity", "someState", "testZipCode", "", county)));
    postExpectingSuccess("homeAddress", Map.of(
        "streetAddress", List.of("originalStreetAddress"),
        "apartmentNumber", List.of("originalApt"),
        "city", List.of("originalCity"),
        "zipCode", List.of("54321"),
        "state", List.of("MN"),
        "sameMailingAddress", List.of()
    ));
    postExpectingSuccess("verifyHomeAddress", "useEnrichedAddress", "true");

    postExpectingSuccess("identifyCountyBeforeApplying", "county", county);
    when(locationClient.validateAddress(any())).thenReturn(
        Optional.of(new Address("smarty street", "City", "CA", "03104", "", county))
    );
    postExpectingSuccess("mailingAddress", Map.of(
        "streetAddress", List.of("someStreetAddress"),
        "apartmentNumber", List.of("someApartmentNumber"),
        "city", List.of("someCity"),
        "zipCode", List.of("12345"),
        "state", List.of("IL"),
        "sameMailingAddress", List.of()
    ));
    postExpectingNextPageElementText("verifyMailingAddress",
        "useEnrichedAddress",
        "true",
        "mailingAddress-address_street",
        "smarty street");
  }


  private void goThroughShortMfipFlow(String county, String nationName, String[] programs)
      throws Exception {
    addHouseholdMembersWithEA();
    getToPersonalInfoScreen(programs);
    addAddressInGivenCounty(county);
    postExpectingSuccess("identifyCountyBeforeApplying", "county", county);
    postExpectingRedirect("tribalNationMember", "isTribalNationMember", "true", "selectTheTribe");
    postExpectingRedirect("selectTheTribe", "selectedTribe", nationName, "applyForMFIP");
  }
}
