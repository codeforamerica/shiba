package org.codeforamerica.shiba.pages;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.Program.CCAP;
import static org.codeforamerica.shiba.Program.EA;
import static org.codeforamerica.shiba.Program.GRH;
import static org.codeforamerica.shiba.Program.SNAP;
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
import org.codeforamerica.shiba.TribalNationRoutingDestination;
import org.codeforamerica.shiba.application.parsers.CountyParser;
import org.codeforamerica.shiba.mnit.CountyRoutingDestination;
import org.codeforamerica.shiba.mnit.RoutingDestination;
import org.codeforamerica.shiba.output.Document;
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
  @Autowired
  private Map<String, TribalNationRoutingDestination> tribalNations;
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
    addHouseholdMembersWithEA();
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
    goThroughShortTribalTanfFlow(nationName, county, "true", EA, CCAP, GRH, SNAP);
    assertRoutingDestinationIsCorrectForDocument(Document.CAF, "Mille Lacs Band of Ojibwe", county);
    assertRoutingDestinationIsCorrectForDocument(Document.UPLOADED_DOC, "Mille Lacs Band of Ojibwe",
        county);
    assertRoutingDestinationIsCorrectForDocument(Document.CCAP, county);
  }

  @ParameterizedTest
  @CsvSource(value = {
      "Becker,true", "Mahnomen,true", "Clearwater,true",
      "Becker,false", "Mahnomen,false", "Clearwater,false"})
  void routeWhiteEarthApplicationsToWhiteEarthOnly(String county, String applyForTribalTanf)
      throws Exception {
    goThroughShortTribalTanfFlow("White Earth", county, applyForTribalTanf, EA, CCAP, GRH, SNAP);

    assertRoutingDestinationIsCorrectForDocument(Document.CAF, WHITE_EARTH);
    assertRoutingDestinationIsCorrectForDocument(Document.CCAP, WHITE_EARTH);
    assertRoutingDestinationIsCorrectForDocument(Document.UPLOADED_DOC, WHITE_EARTH);

    var routingDestinations = routingDecisionService.getRoutingDestinations(applicationData, CAF);
    RoutingDestination routingDestination = routingDestinations.get(0);
    assertThat(routingDestination.getFolderId()).isEqualTo("3b0aa880-db45-483d-fa0-7987c9b0c02d");
    assertThat(routingDestination.getDhsProviderId()).isEqualTo("A086642300");
    assertThat(routingDestination.getEmail()).isEqualTo("amy.littlewolf@whiteearth-nsn.gov");
    assertThat(routingDestination.getPhoneNumber()).isEqualTo("218-935-2359");
  }

  @ParameterizedTest
  @ValueSource(strings = {"Nobles", "Scott", "Meeker"})
  void routeWhiteEarthApplicationsToCountyOnlyAndSeeMfip(String county) throws Exception {
    getToPersonalInfoScreen(EA, CCAP, GRH, SNAP);
    addAddressInGivenCounty(county);
    postExpectingSuccess("identifyCountyBeforeApplying", "county", county);
    postExpectingRedirect("tribalNationMember", "isTribalNationMember", "true", "selectTheTribe");
    postExpectingRedirect("selectTheTribe", "selectedTribe", WHITE_EARTH, "applyForMFIP");

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
    goThroughShortTribalTanfFlow("White Earth", county, applyForTribalTANF, EA);

    assertRoutingDestinationIsCorrectForDocument(Document.CAF, destinationName);
    assertRoutingDestinationIsCorrectForDocument(Document.UPLOADED_DOC, destinationName);
  }

  private void goThroughShortTribalTanfFlow(String nationName, String county,
      String applyForTribalTANF,
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
        applyForTribalTANF,
        applyForTribalTANF.equals("true") ? "tribalTANFConfirmation" : "introIncome");
  }

  @ParameterizedTest
  @CsvSource(value = {
      "Bois Forte,Olmsted",
      "Fond Du Lac,Olmsted",
      "Grand Portage,Olmsted",
      "Leech Lake,Olmsted",
      "Mille Lacs Band of Ojibwe,Olmsted",
      "White Earth,Olmsted",
      "Bois Forte,Aitkin",
      "Fond Du Lac,Benton",
      "Grand Portage,Crow Wing",
      "Leech Lake,Morrison",
      "White Earth,Mille Lacs",
      "Bois Forte,Pine"
  })
  void shouldSkipNationBoundariesPageAndRouteToMfip(String nationName, String county)
      throws Exception {
    postExpectingSuccess("identifyCountyBeforeApplying", "county", county);
    postExpectingRedirect("tribalNationMember", "isTribalNationMember", "true", "selectTheTribe");
    postExpectingRedirect("selectTheTribe", "selectedTribe", nationName, "applyForMFIP");
  }

  @ParameterizedTest
  @CsvSource(value = {
      "Red Lake,Hennepin",
      "Shakopee Mdewakanton,Hennepin"
  })
  void shouldGetBootedFromTheFlowIfLivingOutsideOfNationBoundary(String nationName, String county)
      throws Exception {
    postExpectingSuccess("identifyCountyBeforeApplying", "county", county);
    postExpectingRedirect("tribalNationMember",
        "isTribalNationMember",
        "true",
        "selectTheTribe");
    postExpectingRedirect("selectTheTribe", "selectedTribe", nationName, "nationsBoundary");
    postExpectingRedirect("nationsBoundary",
        "livingInNationBoundary",
        "false",
        "introIncome");
  }

  @ParameterizedTest
  @CsvSource(value = {
      "Prairie Island,Hennepin,true",
      "Shakopee Mdewakanton,Hennepin,true",
      "Lower Sioux,Ramsey,true",
      "Upper Sioux,Ramsey,true"
  })
  void tribesThatCanApplyForMfipIfWithinNationBoundaries(String nationName, String county,
      String livingInNationBoundary)
      throws Exception {
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
  void redLakeCanApplyForTribalTanfIfWithinNationBoundaries() throws Exception {
    postExpectingSuccess("identifyCountyBeforeApplying", "county", "Hennepin");
    postExpectingRedirect("tribalNationMember",
        "isTribalNationMember",
        "true",
        "selectTheTribe");
    postExpectingRedirect("selectTheTribe", "selectedTribe", "Red Lake", "nationsBoundary");
    postExpectingRedirect("nationsBoundary",
        "livingInNationBoundary",
        "true",
        "applyForTribalTANF");
  }

  private void assertRoutingDestinationIsCorrectForDocument(Document document,
      String... expectedNames) {
    List<RoutingDestination> routingDestinations =
        routingDecisionService.getRoutingDestinations(applicationData, document);
    List<String> routingDestinationNames = routingDestinations.stream()
        .map(RoutingDestination::getName).collect(Collectors.toList());
    assertThat(routingDestinationNames).containsExactly(expectedNames);
  }

  private void addAddressInGivenCounty(String county) throws Exception {
    String countyNameWithoutSpaces = county.replace(" ", "");
    County value = County.valueOf(countyNameWithoutSpaces);
    when(countyParser.parse(any())).thenReturn(value);
    when(countyParser.parseCountyInput(any())).thenReturn(county);
    fillOutPersonalInfo();
    fillOutContactInfo();

    when(locationClient.validateAddress(any()))
        .thenReturn(Optional.of(new Address(
            "testStreet",
            "testCity",
            "someState",
            "testZipCode",
            "someApt",
            county)));
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
}
