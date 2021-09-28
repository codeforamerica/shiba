package org.codeforamerica.shiba.pages;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.Program.CCAP;
import static org.codeforamerica.shiba.Program.EA;
import static org.codeforamerica.shiba.Program.GRH;
import static org.codeforamerica.shiba.Program.SNAP;
import static org.codeforamerica.shiba.output.Document.CAF;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.CountyMap;
import org.codeforamerica.shiba.TribalNation;
import org.codeforamerica.shiba.application.parsers.CountyParser;
import org.codeforamerica.shiba.mnit.CountyRoutingDestination;
import org.codeforamerica.shiba.mnit.RoutingDestination;
import org.codeforamerica.shiba.pages.enrichment.Address;
import org.codeforamerica.shiba.testutilities.AbstractShibaMockMvcTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

public class TribalNationsMockMvcTest extends AbstractShibaMockMvcTest {

  @Autowired
  private RoutingDecisionService routingDecisionService;
  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  @Autowired
  private CountyMap<CountyRoutingDestination> countyMap;
  @Autowired
  private Map<String, TribalNation> tribalNations;
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
    addHouseholdMembers();
  }

  @ParameterizedTest
  @CsvSource(value = {
      "Lower Sioux,Otter Tail",
      "Prairie Island,Otter Tail",
      "Red Lake,Otter Tail",
      "Shakopee Mdewakanton,Otter Tail",
      "Upper Sioux,Otter Tail"
  })
  void routingForNationsThatAreNotServicedByMilleLacs(String nationName, String county)
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
  void shouldSkipNationBoundariesPageAndRouteToTribalTanf(String nationName, String county)
      throws Exception {
    goThroughShortTribalTanfFlow(nationName, county);
    assertCountyAndTribalNationRoutingAreCorrect(county, "Mille Lacs Band of Ojibwe");
  }


  private void goThroughShortTribalTanfFlow(String nationName, String county) throws Exception {
      getToPersonalInfoScreen(EA, CCAP, GRH, SNAP);
    addAddressInGivenCounty(county);

    postExpectingRedirect("tribalNationMember",
        "isTribalNationMember",
        "true",
        "selectTheTribe");
    postExpectingRedirect("selectTheTribe",
        "selectedTribe",
        nationName,
        "applyForTribalTANF");
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
      "Red Lake,Hennepin,false",
      "Shakopee Mdewakanton,Hennepin,false"
  })
  void shouldGetBootedFromTheFlow(String nationName, String county, String livingInNationBoundary)
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
        "introIncome");
  }

  @ParameterizedTest
  @CsvSource(value = {
      "Prairie Island,Hennepin,true"
  })
  void fullTribalNationFlowMFIP(String nationName, String county, String livingInNationBoundary)
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

  private void assertCountyAndTribalNationRoutingAreCorrect(String county,
      String tribalNationName) {
    List<RoutingDestination> routingDestinations =
        routingDecisionService.getRoutingDestinations(applicationData, CAF);
    List<String> routingDestinationNames = routingDestinations.stream()
        .map(RoutingDestination::getName)
        .toList();
    assertThat(routingDestinationNames)
        .containsExactly(tribalNationName, county);
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
