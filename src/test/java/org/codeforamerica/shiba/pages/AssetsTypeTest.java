package org.codeforamerica.shiba.pages;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.util.List;
import java.util.Map;
import org.codeforamerica.shiba.testutilities.AbstractShibaMockMvcTest;
import org.codeforamerica.shiba.testutilities.FormPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AssetsTypeTest extends AbstractShibaMockMvcTest {

  @BeforeEach
  protected void setUp() throws Exception {
    super.setUp();
    mockMvc.perform(get("/pages/identifyCountyBeforeApplying").session(session)); // start timer
    postExpectingSuccess("identifyCountyBeforeApplying", "county", "Hennepin");
    postExpectingSuccess("languagePreferences",
        Map.of("writtenLanguage", List.of("ENGLISH"), "spokenLanguage", List.of("ENGLISH"))
    );
  }
  @Test
  void verifyAssetTypesForSNAP() throws Exception {
    completeFlowAssetsTypeAsPerProgram("SNAP","CASH","GRH");
    var page = new FormPage(getPage("assets"));
    assertThat(page.getOptionValues("assets")).containsOnly("VEHICLE","STOCK_BOND","NONE");
    
  }
  @Test
  void verifyAssetTypesForCCAP() throws Exception {
    completeFlowAssetsTypeAsPerProgram("CCAP");
    var page = new FormPage(getPage("assets"));
    assertThat(page.getOptionValues("assets")).containsOnly("VEHICLE","STOCK_BOND","REAL_ESTATE","ONE_MILLION_ASSETS","NONE");
    
  }
  @Test
  void verifyAssetTypesForCERTAINPOPS() throws Exception {
    completeFlowAssetsTypeAsPerProgram("CERTAIN_POPS");
    var page = new FormPage(getPage("assets"));
    assertThat(page.getOptionValues("assets")).containsOnly("VEHICLE","STOCK_BOND","LIFE_INSURANCE","BURIAL_ACCOUNT","OWNERSHIP_BUSINESS","REAL_ESTATE","NONE");
    
  }
  @Test
  void verifyAssetTypesForCERTAINPOPSANDCCAP() throws Exception {
    completeFlowAssetsTypeAsPerProgram("CERTAIN_POPS","CCAP");
    var page = new FormPage(getPage("assets"));
    assertThat(page.getOptionValues("assets")).containsOnly("VEHICLE","STOCK_BOND","REAL_ESTATE","LIFE_INSURANCE","BURIAL_ACCOUNT","OWNERSHIP_BUSINESS","ONE_MILLION_ASSETS","NONE");
    
  }
  @Test
  void verifyAssetSourcePagesForAll() throws Exception {
    completeFlowAssetsTypeAsPerProgram("CERTAIN_POPS","CCAP");
    var page = new FormPage(getPage("assets"));
    assertThat(page.getOptionValues("assets")).containsOnly("VEHICLE","STOCK_BOND","REAL_ESTATE","LIFE_INSURANCE","BURIAL_ACCOUNT","OWNERSHIP_BUSINESS","ONE_MILLION_ASSETS","NONE");
    postExpectingRedirect("assets", "assets", List.of("VEHICLE","STOCK_BOND","REAL_ESTATE","LIFE_INSURANCE","BURIAL_ACCOUNT","OWNERSHIP_BUSINESS","ONE_MILLION_ASSETS"), "vehicleAssetSource");
    assertNavigationRedirectsToCorrectNextPage("vehicleAssetSource", "stockAssetSource");
    assertNavigationRedirectsToCorrectNextPage("stockAssetSource", "lifeInsuranceAssetSource");
    assertNavigationRedirectsToCorrectNextPage("lifeInsuranceAssetSource", "burialAccountAssetSource");
    assertNavigationRedirectsToCorrectNextPage("burialAccountAssetSource", "businessOwnershipAssetSource");
    assertNavigationRedirectsToCorrectNextPage("businessOwnershipAssetSource", "realEstateAssetSource");
    assertNavigationRedirectsToCorrectNextPage("realEstateAssetSource", "oneMillionDollarAssetSource");
    
  }

  @Test
  void verifySavingsIfNoneForAssetsChose() throws Exception {
    completeFlowAssetsTypeAsPerProgram("CERTAIN_POPS", "CCAP");
    var page = new FormPage(getPage("assets"));
    assertThat(page.getOptionValues("assets")).containsOnly("VEHICLE", "STOCK_BOND", "REAL_ESTATE",
        "LIFE_INSURANCE", "BURIAL_ACCOUNT", "OWNERSHIP_BUSINESS", "ONE_MILLION_ASSETS", "NONE");
    postExpectingRedirect("assets", "assets", List.of("None"), "savings");
  }

  private void completeFlowAssetsTypeAsPerProgram(String... programs) throws Exception {
    
    completeFlowFromLandingPageThroughReviewInfo(programs);
    postExpectingRedirect("addHouseholdMembers", "addHouseholdMembers", "true", "startHousehold");
    assertNavigationRedirectsToCorrectNextPage("startHousehold", "householdMemberInfo");
    fillOutHousemateInfo("CCAP");
    // Don't select any children in need of care, should get redirected to preparing meals together
    assertCorrectPageTitle("childrenInNeedOfCare", "Who are the children in need of care?");
    postExpectingRedirect("preparingMealsTogether", "isPreparingMealsTogether", "false",
        "livingSituation");
    postExpectingRedirect("livingSituation", "livingSituation", "UNKNOWN", "goingToSchool");
   /* postExpectingNextPageTitle("goingToSchool", "goingToSchool", "true", "Who is going to school?");*/
    postExpectingRedirect("whoIsGoingToSchool", "pregnant"); // no one is going to school
    completeFlowFromIsPregnantThroughTribalNations(true, programs);
    assertNavigationRedirectsToCorrectNextPage("introIncome", "employmentStatus");
    postExpectingNextPageTitle("employmentStatus", "areYouWorking", "false", "Job Search");
    postExpectingNextPageTitle("jobSearch", "currentlyLookingForJob", "true",
        "Who is looking for a job");
    fillSupportAndCare(programs);
  }

  private void fillSupportAndCare(String... programs) throws Exception {
    assertNavigationRedirectsToCorrectNextPage("incomeUpNext", "unearnedIncome");
    postExpectingRedirect("unearnedIncome", "unearnedIncome", "NO_UNEARNED_INCOME_SELECTED",
        "otherUnearnedIncome");
    postExpectingRedirect("otherUnearnedIncome",
        "otherUnearnedIncome",
        "NO_OTHER_UNEARNED_INCOME_SELECTED",
        "futureIncome");
    fillAdditionalIncomeInfo(programs);
    postExpectingRedirect("supportAndCare", "supportAndCare", "false", "assets");
  }
}
