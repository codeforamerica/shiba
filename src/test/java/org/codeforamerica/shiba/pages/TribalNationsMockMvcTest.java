package org.codeforamerica.shiba.pages;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.County.Beltrami;
import static org.codeforamerica.shiba.Program.CASH;
import static org.codeforamerica.shiba.Program.CCAP;
import static org.codeforamerica.shiba.Program.EA;
import static org.codeforamerica.shiba.Program.GRH;
import static org.codeforamerica.shiba.Program.SNAP;
import static org.codeforamerica.shiba.TribalNation.MilleLacsBandOfOjibwe;
import static org.codeforamerica.shiba.TribalNation.OtherFederallyRecognizedTribe;
import static org.codeforamerica.shiba.TribalNation.RedLakeNation;
import static org.codeforamerica.shiba.TribalNation.WhiteEarth;

import static org.codeforamerica.shiba.output.Document.*;
import static org.codeforamerica.shiba.output.Document.CAF;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.ServicingAgencyMap;
import org.codeforamerica.shiba.TribalNation;
import org.codeforamerica.shiba.mnit.CountyRoutingDestination;
import org.codeforamerica.shiba.mnit.RoutingDestination;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.pages.config.FeatureFlag;
import org.codeforamerica.shiba.pages.enrichment.Address;
import org.codeforamerica.shiba.testutilities.AbstractShibaMockMvcTest;
import org.codeforamerica.shiba.testutilities.FormPage;
import org.codeforamerica.shiba.testutilities.TestApplicationDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;

public class TribalNationsMockMvcTest extends AbstractShibaMockMvcTest {

  @Autowired
  private RoutingDecisionService routingDecisionService;
  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  @Autowired
  private ServicingAgencyMap<CountyRoutingDestination> countyMap;

  @BeforeEach
  protected void setUp() throws Exception {
    super.setUp();
    new TestApplicationDataBuilder(applicationData)
        .withPageData("identifyCounty", "county", "Hennepin");
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
    addHouseholdMembersWithProgram("EA");
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
        .containsExactly(countyMap.get(County.getForName(county)));
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
      "Bois Forte,Chisago",
      "Fond Du Lac,Chisago",
      "Grand Portage,Chisago",
      "Leech Lake,Chisago",
      "Mille Lacs Band of Ojibwe,Chisago",
      "White Earth,Chisago",
      "Bois Forte,Kanabec",
      "Fond Du Lac,Kanabec",
      "Grand Portage,Kanabec",
      "Leech Lake,Kanabec",
      "Mille Lacs Band of Ojibwe,Kanabec",
      "White Earth,Kanabec",
      "Mille Lacs Band of Ojibwe,Ramsey",
      "Mille Lacs Band of Ojibwe,Aitkin",
      "Mille Lacs Band of Ojibwe,Benton",
      "Mille Lacs Band of Ojibwe,Crow Wing",
      "Mille Lacs Band of Ojibwe,Morrison",
      "Mille Lacs Band of Ojibwe,Mille Lacs",
      "Mille Lacs Band of Ojibwe,Pine",
      "Mille Lacs Band of Ojibwe,Chisago",
      "Mille Lacs Band of Ojibwe,Kanabec"
  })
  void shouldSkipNationBoundariesPageAddTribalTanfAndRouteToBothMilleLacsAndCounty(
      String tribalNation, String county)
      throws Exception {
    addHouseholdMembersWithProgram("EA");
    goThroughShortTribalTanfFlow(tribalNation, county, "true", EA, CCAP, GRH, SNAP);
    assertRoutingDestinationIsCorrectForDocument(CAF, "Mille Lacs Band of Ojibwe", county);
    assertRoutingDestinationIsCorrectForDocument(UPLOADED_DOC, "Mille Lacs Band of Ojibwe",
        county);
    assertRoutingDestinationIsCorrectForDocument(Document.CCAP, county);
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
  void shouldAddTribalTanfAndRouteCAFToMilleLacsAndCCAPToCounty(
      String tribalNation, String county)
      throws Exception {
    addHouseholdMembersWithProgram("CCAP");
    goThroughShortTribalTanfFlow(tribalNation, county, "true", CCAP);
    assertRoutingDestinationIsCorrectForDocument(CAF, "Mille Lacs Band of Ojibwe");
    assertRoutingDestinationIsCorrectForDocument(UPLOADED_DOC, "Mille Lacs Band of Ojibwe",
        county);
    assertRoutingDestinationIsCorrectForDocument(Document.CCAP, county);
  }

  @ParameterizedTest
  @CsvSource(value = {"Becker", "Mahnomen", "Clearwater"})
  void routeWhiteEarthApplicationsToWhiteEarthOnlyAndSeeMFIP(String county) throws Exception {
    addHouseholdMembersWithProgram("EA");
    goThroughShortMfipFlow(county, WhiteEarth, new String[]{EA, CCAP, GRH, SNAP});

    assertRoutingDestinationIsCorrectForDocument(CAF, WhiteEarth.toString());
    assertRoutingDestinationIsCorrectForDocument(Document.CCAP, WhiteEarth.toString());
    assertRoutingDestinationIsCorrectForDocument(UPLOADED_DOC, WhiteEarth.toString());

    var routingDestinations = routingDecisionService.getRoutingDestinations(applicationData, CAF);
    RoutingDestination routingDestination = routingDestinations.get(0);
    assertThat(routingDestination.getDhsProviderId()).isEqualTo("A086642300");
    assertThat(routingDestination.getEmail()).isEqualTo("mnbenefits@state.mn.us");
    assertThat(routingDestination.getPhoneNumber()).isEqualTo("218-935-2359");
  }

  @ParameterizedTest
  @ValueSource(strings = {"Nobles", "Scott", "Meeker"})
  void routeWhiteEarthApplicationsToCountyOnlyAndSeeMfip(String county) throws Exception {
    addHouseholdMembersWithProgram("EA");
    goThroughShortMfipFlow(county, WhiteEarth, new String[]{EA, CCAP, GRH, SNAP});

    assertRoutingDestinationIsCorrectForDocument(CAF, county);
    assertRoutingDestinationIsCorrectForDocument(Document.CCAP, county);
    assertRoutingDestinationIsCorrectForDocument(UPLOADED_DOC, county);
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
    addHouseholdMembersWithProgram("EA");

    goThroughShortTribalTanfFlow(WhiteEarth.toString(), county, applyForTribalTANF, EA);

    assertRoutingDestinationIsCorrectForDocument(CAF, destinationName);
    assertRoutingDestinationIsCorrectForDocument(UPLOADED_DOC, destinationName);
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
    addHouseholdMembersWithProgram("EA");
    addAddressInGivenCounty(county);

    postExpectingRedirect("tribalNationMember", "isTribalNationMember", "true", "selectTheTribe");
    postExpectingRedirect("selectTheTribe", "selectedTribe", nationName, "applyForMFIP");

    assertRoutingDestinationIsCorrectForDocument(CAF, expectedRoutingDestination);
    assertRoutingDestinationIsCorrectForDocument(UPLOADED_DOC, expectedRoutingDestination);
  }

  @Test
  void clientsFromOtherFederallyRecognizedNationsShouldBeAbleToApplyForTribalTanfAndRouteToRedLake()
      throws Exception {

    addHouseholdMembersWithProgram("EA");
    goThroughShortTribalTanfFlow(OtherFederallyRecognizedTribe.toString(), Beltrami.toString(),
        "true", EA);
    assertRoutingDestinationIsCorrectForDocument(CAF, RedLakeNation.toString());
    assertRoutingDestinationIsCorrectForDocument(Document.CCAP, RedLakeNation.toString());
    assertRoutingDestinationIsCorrectForDocument(UPLOADED_DOC, RedLakeNation.toString());

  }

  @Test
  void clientsFromOtherFederallyRecognizedNationsShouldBeRoutedToRedLakeEvenIfTheyAreNotApplyingForTanf()
      throws Exception {
    addHouseholdMembersWithProgram("EA");
    goThroughShortTribalTanfFlow(OtherFederallyRecognizedTribe.toString(), Beltrami.toString(),
        "false", EA);
    assertRoutingDestinationIsCorrectForDocument(CAF, RedLakeNation.toString());
    assertRoutingDestinationIsCorrectForDocument(Document.CCAP, RedLakeNation.toString());
    assertRoutingDestinationIsCorrectForDocument(UPLOADED_DOC, RedLakeNation.toString());

  }

  @Test
  void clientsFromOtherFederallyRecognizedNationsShouldBeAbleToApplyForMFIPAndRouteToCounty()
      throws Exception {
    when(featureFlagConfiguration.get("white-earth-and-red-lake-routing"))
        .thenReturn(FeatureFlag.OFF);

    addHouseholdMembersWithProgram("EA");
    goThroughShortTribalTanfFlow(OtherFederallyRecognizedTribe.toString(), Beltrami.toString(),
        "true", EA);
    assertRoutingDestinationIsCorrectForDocument(CAF, Beltrami.toString());
    assertRoutingDestinationIsCorrectForDocument(Document.CCAP, Beltrami.toString());
    assertRoutingDestinationIsCorrectForDocument(UPLOADED_DOC, Beltrami.toString());
  }

  @ParameterizedTest
  @CsvSource(value = {
      "Red Lake,Hennepin",
      "Shakopee Mdewakanton,Hennepin"
  })
  void shouldGetBootedFromTheFlowAndSentToCountyIfLivingOutsideOfNationBoundary(String nationName,
      String county)
      throws Exception {
    addHouseholdMembersWithProgram("EA");

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

    assertRoutingDestinationIsCorrectForDocument(CAF, county);
    assertRoutingDestinationIsCorrectForDocument(UPLOADED_DOC, county);
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
    addHouseholdMembersWithProgram("EA");

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
    addHouseholdMembersWithProgram("EA");
    goThroughLongTribalTanfFlow(RedLakeNation.toString(), "Hennepin", "true", CCAP, SNAP, CASH, EA);

    assertRoutingDestinationIsCorrectForDocument(CAF, RedLakeNation.toString());
    assertRoutingDestinationIsCorrectForDocument(UPLOADED_DOC, RedLakeNation.toString());
    assertRoutingDestinationIsCorrectForDocument(Document.CCAP, RedLakeNation.toString());
  }

  @Test
  void redLakeApplicationsWithOnlySnapGetSentToRedLake() throws Exception {
    fillOutPersonalInfo();
    addHouseholdMembersWithProgram(SNAP);
    goThroughLongTribalTanfFlow(RedLakeNation.toString(), "Hennepin", "false", SNAP);

    assertRoutingDestinationIsCorrectForDocument(CAF, RedLakeNation.toString());
    assertRoutingDestinationIsCorrectForDocument(UPLOADED_DOC, RedLakeNation.toString());
    assertRoutingDestinationIsCorrectForDocument(Document.CCAP, RedLakeNation.toString());
  }

  @Test
  void redLakeApplicationsWithGrhAndTribalTanfGetSentToRedLakeAndCounty() throws Exception {
    fillOutPersonalInfo();
    addHouseholdMembersWithProgram("GRH");

    String county = "Olmsted";
    goThroughLongTribalTanfFlow(RedLakeNation.toString(), county, "true", GRH);
    assertRoutingDestinationIsCorrectForDocument(CAF, county, RedLakeNation.toString());
    assertRoutingDestinationIsCorrectForDocument(UPLOADED_DOC, county,
        RedLakeNation.toString());
    assertRoutingDestinationIsCorrectForDocument(Document.CCAP, county, RedLakeNation.toString());
  }

  @Test
  void redLakeApplicationsWithGrhAndSnapAndTribalTanfGetSentToRedLakeAndCounty() throws Exception {
    fillOutPersonalInfo();
    addHouseholdMembersWithProgram(SNAP);

    String county = "Olmsted";
    goThroughLongTribalTanfFlow(RedLakeNation.toString(), county, "true", GRH);
    assertRoutingDestinationIsCorrectForDocument(CAF, county, RedLakeNation.toString());
    assertRoutingDestinationIsCorrectForDocument(UPLOADED_DOC, county,
        RedLakeNation.toString());
    assertRoutingDestinationIsCorrectForDocument(Document.CCAP, county, RedLakeNation.toString());
  }

  @Test
  void redLakeApplicationsWithOnlyGrhAndCcapGetSentToRedLakeAndCounty() throws Exception {
    fillOutPersonalInfo();
    addHouseholdMembersWithProgram(CCAP);

    String county = "Olmsted";
    goThroughLongTribalTanfFlow(RedLakeNation.toString(), county, "false", GRH);

    assertRoutingDestinationIsCorrectForDocument(CAF, county, RedLakeNation.toString());
    assertRoutingDestinationIsCorrectForDocument(UPLOADED_DOC, county,
        RedLakeNation.toString());
    assertRoutingDestinationIsCorrectForDocument(Document.CCAP, county, RedLakeNation.toString());
  }

  @Test
  void redLakeApplicationsWithGrhOnlyGetSentToCounty() throws Exception {
    addHouseholdMembersWithProgram("GRH");
    getToPersonalInfoScreen(GRH);
    fillOutPersonalInfo();
    String county = "Anoka";
    addAddressInGivenCounty(county);
    postExpectingRedirect("tribalNationMember",
        "isTribalNationMember",
        "true",
        "selectTheTribe");

    postExpectingRedirect("selectTheTribe", "selectedTribe", RedLakeNation.toString(),
        "nationsBoundary");
    postExpectingRedirect("nationsBoundary",
        "livingInNationBoundary",
        "false",
        "introIncome");

    assertRoutingDestinationIsCorrectForDocument(CAF, county);
    assertRoutingDestinationIsCorrectForDocument(UPLOADED_DOC, county);
    assertRoutingDestinationIsCorrectForDocument(Document.CCAP, county);
  }

  @Test
  void redLakeApplicationsGetSentToCountyIfFeatureFlagIsTurnedOff() throws Exception {
    when(featureFlagConfiguration.get("white-earth-and-red-lake-routing")).thenReturn(
        FeatureFlag.OFF);
    fillOutPersonalInfo();
    addHouseholdMembersWithProgram(CCAP);

    String county = "Olmsted";
    goThroughLongTribalTanfFlow(RedLakeNation.toString(), county, "false", GRH);

    assertRoutingDestinationIsCorrectForDocument(CAF, county);
    assertRoutingDestinationIsCorrectForDocument(UPLOADED_DOC, county);
    assertRoutingDestinationIsCorrectForDocument(Document.CCAP, county);
  }

  @Test
  void whiteEarthApplicationsGetSentToCountyAndMilleLacsIfFeatureFlagIsTurnedOff()
      throws Exception {
    when(featureFlagConfiguration.get("white-earth-and-red-lake-routing")).thenReturn(
        FeatureFlag.OFF);

    addHouseholdMembersWithProgram("EA");
    String county = "Becker";
    goThroughShortMfipFlow(county, WhiteEarth, new String[]{EA, CCAP, GRH, SNAP});

    assertRoutingDestinationIsCorrectForDocument(CAF, MilleLacsBandOfOjibwe.toString(),
        county);
    assertRoutingDestinationIsCorrectForDocument(Document.CCAP, county);

    assertRoutingDestinationIsCorrectForDocument(UPLOADED_DOC,
        MilleLacsBandOfOjibwe.toString(),
        county);
  }

  @Test
  void whiteEarthApplicationsGetSentToCountyOnlyIfFeatureFlagIsTurnedOff() throws Exception {
    when(featureFlagConfiguration.get("white-earth-and-red-lake-routing")).thenReturn(
        FeatureFlag.OFF);

    addHouseholdMembersWithProgram("CCAP");
    String county = "Becker";
    goThroughShortMfipFlow(county, WhiteEarth, new String[]{CCAP});

    assertRoutingDestinationIsCorrectForDocument(CAF, county);
    assertRoutingDestinationIsCorrectForDocument(Document.CCAP, county);
    assertRoutingDestinationIsCorrectForDocument(UPLOADED_DOC, county);
  }

  @Test
  void whiteEarthApplicationsGetSentToMilleLacsAndCountyIfFeatureFlagIsTurnedOff()
      throws Exception {
    when(featureFlagConfiguration.get("white-earth-and-red-lake-routing")).thenReturn(
        FeatureFlag.OFF);

    String county = "Hennepin";
    addHouseholdMembersWithProgram("EA");
    goThroughShortTribalTanfFlow(WhiteEarth.toString(), county, "true", EA, CCAP, GRH, SNAP);
    assertRoutingDestinationIsCorrectForDocument(CAF, MilleLacsBandOfOjibwe.toString(),
        county);
    assertRoutingDestinationIsCorrectForDocument(UPLOADED_DOC,
        MilleLacsBandOfOjibwe.toString(),
        county);
    assertRoutingDestinationIsCorrectForDocument(Document.CCAP, county);
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
        "true",
        "tribalTANFConfirmation");
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
    new TestApplicationDataBuilder(applicationData)
        .withPageData("identifyCounty", "county", county);
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

    postExpectingSuccess("verifyMailingAddress", "useEnrichedAddress", "true");

    var returnPage = new FormPage(getPage("reviewInfo"));
    assertThat(returnPage.getElementTextById("mailingAddress-address_street")).isEqualTo(
        "smarty street");
  }

  private void goThroughShortMfipFlow(String county, TribalNation tribalNation, String[] programs)
      throws Exception {
    getToPersonalInfoScreen(programs);
    addAddressInGivenCounty(county);
    postExpectingSuccess("identifyCountyBeforeApplying", "county", county);
    postExpectingRedirect("tribalNationMember", "isTribalNationMember", "true", "selectTheTribe");
    postExpectingRedirect("selectTheTribe", "selectedTribe", tribalNation.toString(),
        "applyForMFIP");
  }
}
