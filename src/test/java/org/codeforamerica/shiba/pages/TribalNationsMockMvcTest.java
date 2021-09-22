package org.codeforamerica.shiba.pages;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.util.List;
import java.util.Map;
import org.codeforamerica.shiba.testutilities.AbstractShibaMockMvcTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class TribalNationsMockMvcTest extends AbstractShibaMockMvcTest {

  @BeforeEach
  protected void setUp() throws Exception {
    super.setUp();
    mockMvc.perform(get("/pages/identifyCountyBeforeApplying").session(session)); // start timer
    postExpectingSuccess("languagePreferences",
        Map.of("writtenLanguage", List.of("ENGLISH"), "spokenLanguage", List.of("ENGLISH"))
    );
    addHouseholdMembers();
  }

  @ParameterizedTest
  @CsvSource(value = {
      "Bois Forte,Hennepin",
      "Fond Du Lac,Hennepin",
      "Grand Portage,Hennepin",
      "Leech Lake,Hennepin",
      "Mille Lacs Band of Ojibwe,Hennepin",
//      "White Earth,Hennepin",
      "Bois Forte,Anoka",
      "Fond Du Lac,Anoka",
      "Grand Portage,Anoka",
      "Leech Lake,Anoka",
      "Mille Lacs Band of Ojibwe,Anoka",
//      "White Earth,Anoka",
      "Bois Forte,Ramsey",
      "Fond Du Lac,Ramsey",
      "Grand Portage,Ramsey",
      "Leech Lake,Ramsey",
      "Mille Lacs Band of Ojibwe,Ramsey",
//      "White Earth,Ramsey",
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
    postExpectingSuccess("identifyCountyBeforeApplying", "county", county);
    postExpectingRedirect("tribalNationMember", "isTribalNationMember", "true", "selectTheTribe");
    postExpectingRedirect("selectTheTribe", "selectedTribe", nationName, "applyForTribalTANF");
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
}
