package org.codeforamerica.shiba.output.pdf;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.codeforamerica.shiba.output.Document.CERTAIN_POPS;
import static org.codeforamerica.shiba.output.caf.CoverPagePreparer.CHILDCARE_WAITING_LIST_UTM_SOURCE;
import static org.codeforamerica.shiba.testutilities.TestUtils.assertPdfFieldEquals;
import static org.codeforamerica.shiba.testutilities.TestUtils.assertPdfFieldIsEmpty;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.codeforamerica.shiba.Program;
import org.codeforamerica.shiba.pages.enrichment.Address;
import org.codeforamerica.shiba.testutilities.AbstractShibaMockMvcTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("pdf")
public class PdfMockMvcTest extends AbstractShibaMockMvcTest {

  @Override
  @BeforeEach
  protected void setUp() throws Exception {
    super.setUp();
    mockMvc.perform(get("/pages/identifyCountyBeforeApplying").session(session)); // start timer
    postExpectingSuccess("identifyCountyBeforeApplying", "county", "Hennepin");
    postExpectingSuccess("languagePreferences", Map.of("writtenLanguage", List.of("ENGLISH"),
        "spokenLanguage", List.of("ENGLISH"), "needInterpreter", List.of("true")));

    postExpectingSuccess("addHouseholdMembers", "addHouseholdMembers", "false");
  }

  @Test
  void shouldAnswerEnergyAssistanceQuestion() throws Exception {
    selectPrograms("CASH");

    postExpectingSuccess("energyAssistance", "energyAssistance", "true");
    postExpectingSuccess("energyAssistanceMoreThan20", "energyAssistanceMoreThan20", "false");

    var caf = submitAndDownloadCaf();
    assertPdfFieldEquals("RECEIVED_LIHEAP", "No", caf);
  }

  @Test
  void shouldMapEnergyAssistanceWhenUserReceivedNoAssistance() throws Exception {
    selectPrograms("CASH");

    postExpectingSuccess("energyAssistance", "energyAssistance", "false");

    var caf = submitAndDownloadCaf();
    assertPdfFieldEquals("RECEIVED_LIHEAP", "No", caf);
  }

  @Test
  void shouldSupportCyrillicCharacters() throws Exception {
    selectPrograms("CASH");

    postToUrlExpectingSuccess("/submit", "/pages/signThisApplication/navigation",
        Map.of("applicantSignature", List.of("aЕкатерина")));
    var caf = downloadCafClientPDF();

    assertPdfFieldEquals("APPLICANT_SIGNATURE", "aЕкатерина", caf);
  }

  @Test
  void shouldMapChildrenNeedingChildcareFullNames() throws Exception {
    selectPrograms("CCAP");
    addHouseholdMembersWithCCAP();

    String jimHalpertId = getFirstHouseholdMemberId();
    postExpectingSuccess("childrenInNeedOfCare", "whoNeedsChildCare",
        List.of("Dwight Schrute applicant", "Jim Halpert " + jimHalpertId));

    postExpectingSuccess("whoHasParentNotAtHome", "whoHasAParentNotLivingAtHome",
        List.of("Dwight Schrute applicant", "Jim Halpert " + jimHalpertId));
    postExpectingSuccess("parentNotAtHomeNames", Map.of("whatAreTheParentsNames",
        List.of("", "Jim's Parent"), "childIdMap", List.of("applicant", jimHalpertId)));

    var ccap = submitAndDownloadCcap();
    assertPdfFieldEquals("CHILD_NEEDS_CHILDCARE_FULL_NAME_0", "Dwight Schrute", ccap);
    assertPdfFieldEquals("CHILD_NEEDS_CHILDCARE_FULL_NAME_1", "Jim Halpert", ccap);
    assertPdfFieldEquals("CHILD_FULL_NAME_0", "Dwight Schrute", ccap);
    assertPdfFieldIsEmpty("PARENT_NOT_LIVING_AT_HOME_0", ccap);
    assertPdfFieldEquals("CHILD_FULL_NAME_1", "Jim Halpert", ccap);
    assertPdfFieldEquals("PARENT_NOT_LIVING_AT_HOME_1", "Jim's Parent", ccap);
    assertPdfFieldIsEmpty("CHILD_FULL_NAME_2", ccap);
    assertPdfFieldIsEmpty("PARENT_NOT_LIVING_AT_HOME_2", ccap);
  }

  @Test
  void shouldNotMapParentsLivingOutsideOfHomeIfNoneSelected() throws Exception {
    selectPrograms("CCAP");

    addHouseholdMembersWithCCAP();

    postExpectingSuccess("childrenInNeedOfCare", "whoNeedsChildCare",
        List.of("Dwight Schrute applicant", getJimFullNameAndId()));

    postExpectingSuccess("whoHasParentNotAtHome", "whoHasAParentNotLivingAtHome",
        "NONE_OF_THE_ABOVE");

    var ccap = submitAndDownloadCcap();
    assertPdfFieldEquals("CHILD_NEEDS_CHILDCARE_FULL_NAME_0", "Dwight Schrute", ccap);
    assertPdfFieldEquals("CHILD_NEEDS_CHILDCARE_FULL_NAME_1", "Jim Halpert", ccap);
    assertPdfFieldIsEmpty("CHILD_FULL_NAME_0", ccap);
    assertPdfFieldIsEmpty("PARENT_NOT_LIVING_AT_HOME_0", ccap);
    assertPdfFieldIsEmpty("CHILD_FULL_NAME_1", ccap);
    assertPdfFieldIsEmpty("PARENT_NOT_LIVING_AT_HOME_1", ccap);
    assertPdfFieldIsEmpty("CHILD_FULL_NAME_2", ccap);
    assertPdfFieldIsEmpty("PARENT_NOT_LIVING_AT_HOME_2", ccap);
  }

  @Test
  void shouldDefaultToNoForMillionDollarQuestionWhenQuestionPageIsNotShown() throws Exception {
    selectPrograms("CCAP");

    postExpectingSuccess("energyAssistance", "energyAssistance", "false");
    postExpectingSuccess("medicalExpenses", "medicalExpenses", "NONE_OF_THE_ABOVE");
    postExpectingSuccess("supportAndCare", "supportAndCare", "false");
    postExpectingSuccess("assets", "assets", "NONE");
    postExpectingSuccess("savings", "haveSavings", "false");

    var ccap = submitAndDownloadCcap();
    assertPdfFieldEquals("HAVE_MILLION_DOLLARS", "No", ccap);
  }

  @Test
  void shouldMarkYesForMillionDollarQuestionWhenChoiceIsYes() throws Exception {
    selectPrograms("CCAP");

    postExpectingSuccess("energyAssistance", "energyAssistance", "false");
    postExpectingSuccess("medicalExpenses", "medicalExpenses", "NONE_OF_THE_ABOVE");
    postExpectingSuccess("supportAndCare", "supportAndCare", "false");
    postExpectingSuccess("assets", "assets", List.of("STOCK_BOND", "ONE_MILLION_ASSETS"));
    postExpectingSuccess("savings", "haveSavings", "false");
    
    var ccap = submitAndDownloadCcap();
    assertPdfFieldEquals("HAVE_MILLION_DOLLARS", "Yes", ccap);
  }

  @Test
  void shouldNotMapUnearnedIncomeCcapWhenNoneOfTheAboveIsSelected() throws Exception {
    selectPrograms("CCAP");
    fillInRequiredPages();
    postExpectingSuccess("otherUnearnedIncome", "otherUnearnedIncome",
        "NO_OTHER_UNEARNED_INCOME_SELECTED");

    var ccap = submitAndDownloadCcap();
    assertPdfFieldEquals("BENEFITS", "No", ccap);
    assertPdfFieldEquals("INSURANCE_PAYMENTS", "No", ccap);
    assertPdfFieldEquals("CONTRACT_FOR_DEED", "No", ccap);
    assertPdfFieldEquals("TRUST_MONEY", "No", ccap);
    assertPdfFieldEquals("HEALTH_CARE_REIMBURSEMENT", "No", ccap);
    assertPdfFieldEquals("INTEREST_DIVIDENDS", "No", ccap);
    assertPdfFieldEquals("OTHER_PAYMENTS", "No", ccap);
  }

  @Test
  void shouldMapAdultsInHouseholdRequestingChildcareAssistance() throws Exception {
    selectPrograms("CCAP");

    addHouseholdMembersWithCCAP();

    String jim = getJimFullNameAndId();
    postExpectingSuccess("childrenInNeedOfCare", "whoNeedsChildCare", jim);

    postExpectingSuccess("jobSearch", "currentlyLookingForJob", "true");
    String pam = getPamFullNameAndId();
    postExpectingSuccess("whoIsLookingForAJob", "whoIsLookingForAJob", List.of(jim, pam));

    String me = getApplicantFullNameAndId();
    postExpectingSuccess("whoIsGoingToSchool", "whoIsGoingToSchool", List.of(me, jim));

    // Add a job for Jim
    addFirstJob(jim, "Jim's Employer");

    // Add a job for Pam
    postWithQueryParam("jobBuilder", "option", "0");
    addJob(pam, "Pam's Employer");

    var ccap = submitAndDownloadCcap();
    assertPdfFieldEquals("ADULT_REQUESTING_CHILDCARE_LOOKING_FOR_JOB_FULL_NAME_0", "Pam Beesly",
        ccap);
    assertPdfFieldEquals("ADULT_REQUESTING_CHILDCARE_GOING_TO_SCHOOL_FULL_NAME_0", "Dwight Schrute",
        ccap);
    assertPdfFieldEquals("ADULT_REQUESTING_CHILDCARE_WORKING_FULL_NAME_0", "Pam Beesly", ccap);
    assertPdfFieldEquals("ADULT_REQUESTING_CHILDCARE_WORKING_EMPLOYERS_NAME_0", "Pam's Employer",
        ccap);
    assertPdfFieldIsEmpty("ADULT_REQUESTING_CHILDCARE_LOOKING_FOR_JOB_FULL_NAME_1", ccap);
    assertPdfFieldIsEmpty("ADULT_REQUESTING_CHILDCARE_GOING_TO_SCHOOL_FULL_NAME_1", ccap);
    assertPdfFieldIsEmpty("ADULT_REQUESTING_CHILDCARE_WORKING_FULL_NAME_1", ccap);
    assertPdfFieldIsEmpty("ADULT_REQUESTING_CHILDCARE_WORKING_EMPLOYERS_NAME_1", ccap);
  }

  private void addFirstJob(String householdMemberNameAndId, String employersName) throws Exception {
    postWithQueryParam("incomeByJob", "option", "0");
    addJob(householdMemberNameAndId, employersName);
  }

  @Test
  void shouldMapJobLastThirtyDayIncomeAllBlankIsUndetermined() throws Exception {
    selectPrograms("CASH");
    addHouseholdMembersWithCCAP();
    fillInRequiredPages();

    // Add a job for Jim
    postWithQueryParam("incomeByJob", "option", "0");
    String jim = getJimFullNameAndId();
    postExpectingSuccess("householdSelectionForIncome", "whoseJobIsIt", jim);
    postExpectingSuccess("employersName", "employersName", "someEmployerName");
    postExpectingSuccess("selfEmployment", "selfEmployment", "false");
    postExpectingSuccess("lastThirtyDaysJobIncome", "lastThirtyDaysJobIncome", "");

    // Add a job for Dwight
    postWithQueryParam("incomeByJob", "option", "0");
    String me = getApplicantFullNameAndId();
    postExpectingSuccess("householdSelectionForIncome", "whoseJobIsIt", me);
    postExpectingSuccess("employersName", "employersName", "someEmployerName");
    postExpectingSuccess("selfEmployment", "selfEmployment", "false");
    postExpectingSuccess("lastThirtyDaysJobIncome", "lastThirtyDaysJobIncome", "");

    var caf = submitAndDownloadCaf();
    assertPdfFieldIsEmpty("SNAP_EXPEDITED_ELIGIBILITY", caf);
  }

  @Test
  void shouldNotAddAuthorizedRepFieldsIfNo() throws Exception {
    selectPrograms("CASH");
    postExpectingSuccess("authorizedRep", "communicateOnYourBehalf", "false");

    var caf = submitAndDownloadCaf();
    assertPdfFieldEquals("AUTHORIZED_REP_FILL_OUT_FORM", "Off", caf);
    assertPdfFieldEquals("AUTHORIZED_REP_GET_NOTICES", "Off", caf);
    assertPdfFieldEquals("AUTHORIZED_REP_SPEND_ON_YOUR_BEHALF", "Off", caf);
  }

  @Test
  void shouldMapRecognizedUtmSourceCCAP() throws Exception {
    selectPrograms("CCAP");

    getWithQueryParam("identifyCountyBeforeApplying", "utm_source",
        CHILDCARE_WAITING_LIST_UTM_SOURCE);
    fillInRequiredPages();

    var ccap = submitAndDownloadCcap();
    assertPdfFieldEquals("UTM_SOURCE", "FROM BSF WAITING LIST", ccap);
  }

  @Test
  void shouldNotMapRecognizedUtmSourceCAF() throws Exception {
    selectPrograms("CASH");
    getWithQueryParam("identifyCountyBeforeApplying", "utm_source",
        CHILDCARE_WAITING_LIST_UTM_SOURCE);
    var caf = submitAndDownloadCaf();
    assertPdfFieldIsEmpty("UTM_SOURCE", caf);
  }

  private void testThatCorrectCountyInstructionsAreDisplayed(String city, String zip,
      String expectedCountyInstructions) throws Exception {
    postExpectingSuccess("homeAddress",
        Map.of("streetAddress", List.of("2168 7th Ave"), "city", List.of(city), "zipCode",
            List.of(zip), "state", List.of("MN"), "sameMailingAddress", List.of("true")));
    postExpectingSuccess("verifyHomeAddress", "useEnrichedAddress", "false");

    var ccap = submitAndDownloadCcap();
    assertPdfFieldEquals("COUNTY_INSTRUCTIONS", expectedCountyInstructions, ccap);
  }

  @Nested
  @Tag("pdf")
  class CAFandCCAP {

    @BeforeEach
    void setUp() throws Exception {
      selectPrograms("SNAP", "CCAP", "CASH");
    }

    @Test
    void shouldMapOriginalAddressIfHomeAddressDoesNotUseEnrichedAddress() throws Exception {
      String originalStreetAddress = "originalStreetAddress";
      String originalApt = "originalApt";
      String originalCity = "originalCity";
      String originalZipCode = "54321";
      postExpectingSuccess("homeAddress",
          Map.of("streetAddress", List.of(originalStreetAddress), "apartmentNumber",
              List.of(originalApt), "city", List.of(originalCity), "zipCode",
              List.of(originalZipCode), "state", List.of("MN"), "sameMailingAddress",
              List.of("false")));
      postExpectingSuccess("verifyHomeAddress", "useEnrichedAddress", "false");

      var ccap = submitAndDownloadCcap();
      assertPdfFieldEquals("APPLICANT_HOME_STREET_ADDRESS", originalStreetAddress, ccap);
      assertPdfFieldEquals("APPLICANT_HOME_CITY", originalCity, ccap);
      assertPdfFieldEquals("APPLICANT_HOME_STATE", "MN", ccap);
      assertPdfFieldEquals("APPLICANT_HOME_ZIPCODE", originalZipCode, ccap);
    }

    @Test
    void shouldMapNoForSelfEmployment() throws Exception {
      addFirstJob(getApplicantFullNameAndId(), "someEmployerName");

      var caf = submitAndDownloadCaf();
      assertPdfFieldEquals("SELF_EMPLOYED", "No", caf);

      var ccap = downloadCcapClientPDF();
      assertPdfFieldEquals("NON_SELF_EMPLOYMENT_EMPLOYERS_NAME_0", "someEmployerName", ccap);
      assertPdfFieldEquals("NON_SELF_EMPLOYMENT_PAY_FREQUENCY_0", "Every week", ccap);
      assertPdfFieldEquals("NON_SELF_EMPLOYMENT_GROSS_MONTHLY_INCOME_0", "4.00", ccap);
    }

    @Test
    void shouldMapEnrichedAddressIfHomeAddressUsesEnrichedAddress() throws Exception {
      String enrichedStreetValue = "testStreet";
      String enrichedCityValue = "testCity";
      String enrichedZipCodeValue = "testZipCode";
      String enrichedApartmentNumber = "someApt";
      String enrichedState = "someState";
      when(locationClient.validateAddress(any()))
          .thenReturn(Optional.of(new Address(enrichedStreetValue, enrichedCityValue, enrichedState,
              enrichedZipCodeValue, enrichedApartmentNumber, "Hennepin")));

      postExpectingSuccess("homeAddress",
          Map.of("streetAddress", List.of("originalStreetAddress"), "apartmentNumber",
              List.of("originalApt"), "city", List.of("originalCity"), "zipCode", List.of("54321"),
              "state", List.of("MN"), "sameMailingAddress", List.of()));

      postExpectingSuccess("verifyHomeAddress", "useEnrichedAddress", "true");

      var caf = submitAndDownloadCaf();
      var ccap = downloadCcapClientPDF();

      List.of(caf, ccap).forEach(pdf -> {
        assertPdfFieldEquals("APPLICANT_HOME_STREET_ADDRESS", enrichedStreetValue, pdf);
        assertPdfFieldEquals("APPLICANT_HOME_CITY", enrichedCityValue, pdf);
        assertPdfFieldEquals("APPLICANT_HOME_STATE", enrichedState, pdf);
        assertPdfFieldEquals("APPLICANT_HOME_ZIPCODE", enrichedZipCodeValue, pdf);
      });

      assertPdfFieldEquals("APPLICANT_HOME_APT_NUMBER", enrichedApartmentNumber, caf);
    }

    @Test
    void shouldMapFullEmployeeNames() throws Exception {
      addHouseholdMembersWithCCAP();
      String jim = getJimFullNameAndId();
      addFirstJob(jim, "someEmployerName");

      var caf = submitAndDownloadCaf();
      var ccap = downloadCcapClientPDF();

      assertPdfFieldEquals("EMPLOYEE_FULL_NAME_0", "Jim Halpert", caf);
      assertPdfFieldEquals("NON_SELF_EMPLOYMENT_EMPLOYEE_FULL_NAME_0", "Jim Halpert", ccap);
    }

    @Test
    void shouldMapJobLastThirtyDayIncomeSomeBlankIsDetermined() throws Exception {
      addHouseholdMembersWithCCAP();

      fillInRequiredPages();

      // Add a job for Jim
      postWithQueryParam("incomeByJob", "option", "0");
      String jim = getJimFullNameAndId();
      postExpectingSuccess("householdSelectionForIncome", "whoseJobIsIt", jim);
      postExpectingSuccess("employersName", "employersName", "someEmployerName");
      postExpectingSuccess("selfEmployment", "selfEmployment", "false");
      postExpectingSuccess("lastThirtyDaysJobIncome", "lastThirtyDaysJobIncome", "123");

      // Add a job for Dwight
      postWithQueryParam("incomeByJob", "option", "0");
      String me = getApplicantFullNameAndId();
      postExpectingSuccess("householdSelectionForIncome", "whoseJobIsIt", me);
      postExpectingSuccess("employersName", "employersName", "someEmployerName");
      postExpectingSuccess("selfEmployment", "selfEmployment", "false");
      postExpectingSuccess("lastThirtyDaysJobIncome", "lastThirtyDaysJobIncome", "");

      var caf = submitAndDownloadCaf();
      var ccap = downloadCcapClientPDF();

      assertPdfFieldEquals("GROSS_MONTHLY_INCOME_0", "123.00", caf);
      assertPdfFieldEquals("MONEY_MADE_LAST_MONTH", "123.00", caf);
      assertPdfFieldEquals("SNAP_EXPEDITED_ELIGIBILITY", "SNAP", caf);

      assertPdfFieldEquals("NON_SELF_EMPLOYMENT_GROSS_MONTHLY_INCOME_0", "123.00", ccap);
    }

    @Test
    void shouldMapLivingSituationToUnknownIfNoneOfTheseIsSelectedAndShouldNotMapTemporarilyWithFriendsOrFamilyYesNo()
        throws Exception {
      fillInRequiredPages();

      postExpectingSuccess("livingSituation", "livingSituation", "UNKNOWN");

      var caf = submitAndDownloadCaf();
      var ccap = downloadCcapClientPDF();

      assertPdfFieldEquals("LIVING_SITUATION", "UNKNOWN", caf);
      assertPdfFieldEquals("LIVING_SITUATION", "UNKNOWN", ccap);
      assertPdfFieldEquals("LIVING_WITH_FAMILY_OR_FRIENDS", "Off", ccap);
    }

    @Test
    void shouldMapLivingSituationToUnknownIfNotAnswered() throws Exception {
      fillInRequiredPages();
      postWithoutData("livingSituation");

      var caf = submitAndDownloadCaf();
      var ccap = downloadCcapClientPDF();

      assertPdfFieldEquals("LIVING_SITUATION", "UNKNOWN", caf);
      assertPdfFieldEquals("LIVING_SITUATION", "UNKNOWN", ccap);
    }

    @Test
    void shouldMapLivingWithFamilyAndFriendsDueToEconomicHardship() throws Exception {
      fillInRequiredPages();
      postExpectingSuccess("livingSituation", "livingSituation",
          "TEMPORARILY_WITH_FRIENDS_OR_FAMILY_DUE_TO_ECONOMIC_HARDSHIP");

      var caf = submitAndDownloadCaf();
      var ccap = downloadCcapClientPDF();

      assertPdfFieldEquals("LIVING_SITUATION", "TEMPORARILY_WITH_FRIENDS_OR_FAMILY", ccap);
      assertPdfFieldEquals("LIVING_SITUATION", "TEMPORARILY_WITH_FRIENDS_OR_FAMILY", caf);
      assertPdfFieldEquals("LIVING_WITH_FAMILY_OR_FRIENDS", "Yes", ccap);
    }

    @Test
    void shouldMapTribalNationMemberYesOrNoAndWhichTribalNation() throws Exception {
      fillInRequiredPages();
      postExpectingSuccess("nationsBoundary", "livingInNationBoundary", "Yes");
      postExpectingSuccess("selectTheTribe", "selectedTribe", "Leech Lake");

      var caf = submitAndDownloadCaf();

      assertPdfFieldEquals("IS_TRIBAL_NATION_MEMBER", "Yes", caf);
      assertPdfFieldEquals("WHICH_TRIBAL_NATION", "Leech Lake", caf);
    }

    @Test
    void shouldMapTribalTANF() throws Exception {
      fillInRequiredPages();
      postExpectingSuccess("applyForTribalTANF", "applyForTribalTANF", "true");

      var caf = submitAndDownloadCaf();

      assertPdfFieldEquals("PROGRAMS", "SNAP, CCAP, CASH, TRIBAL TANF", caf);
    }

    @Test
    void shouldMapMFIP() throws Exception {
      fillInRequiredPages();
      selectPrograms("SNAP");
      postExpectingSuccess("applyForMFIP", "applyForMFIP", "true");

      var caf = submitAndDownloadCaf();

      assertPdfFieldEquals("PROGRAMS", "SNAP, CASH", caf);
    }

    @Test
    void shouldNotMapCashTwiceIfCashAndMFIP() throws Exception {
      fillInRequiredPages();
      postExpectingSuccess("applyForMFIP", "applyForMFIP", "true");

      var caf = submitAndDownloadCaf();

      assertPdfFieldEquals("PROGRAMS", "SNAP, CCAP, CASH", caf);
    }

    @Test
    void shouldMapProgramSelections() throws Exception {
      fillInRequiredPages();
      selectPrograms("SNAP", "CASH", "EA");

      var caf = submitAndDownloadCaf();

      assertPdfFieldEquals("FOOD", "Yes", caf);
      assertPdfFieldEquals("CASH", "Yes", caf);
      assertPdfFieldEquals("EMERGENCY", "Yes", caf);
      assertPdfFieldEquals("CCAP", "Off", caf);
      assertPdfFieldEquals("GRH", "Off", caf);
      assertPdfFieldEquals("PROGRAM_NONE", "Off", caf);
    }

    @Test
    void shouldMapProgramNoneSelection() throws Exception {
      fillInRequiredPages();
      selectPrograms("NONE");
      fillOutHousemateInfo("SNAP");

      var caf = submitAndDownloadCaf();

      assertPdfFieldEquals("FOOD", "Off", caf);
      assertPdfFieldEquals("CASH", "Off", caf);
      assertPdfFieldEquals("EMERGENCY", "Off", caf);
      assertPdfFieldEquals("CCAP", "Off", caf);
      assertPdfFieldEquals("GRH", "Off", caf);
      assertPdfFieldEquals("PROGRAM_NONE", "Yes", caf);
    }
    
    @Test
    void shouldMapHHMemberMoreThan5LessThan10() throws Exception {
      fillInRequiredPages();
      selectPrograms("SNAP");
      fillOutHousemateInfoMoreThanFiveLessThanTen(9);
      var caf = submitAndDownloadCaf();
      assertPdfFieldEquals("FIRST_NAME_4", "householdMemberFirstName4", caf);
      assertPdfFieldEquals("LAST_NAME_4", "householdMemberLastName4", caf);
      assertPdfFieldEquals("OTHER_NAME_4", "houseHoldyMcMemberson4", caf);
      assertPdfFieldEquals("FOOD_4", "Yes", caf);
      assertPdfFieldEquals("RELATIONSHIP_4","housemate", caf);
      assertPdfFieldEquals("DATE_OF_BIRTH_4", "09/14/1950", caf);
      assertPdfFieldEquals("SSN_4", "XXX-XX-XXXX", caf);
      assertPdfFieldEquals("MARITAL_STATUS_4", "NEVER_MARRIED", caf);
      assertPdfFieldEquals("SEX_4", "MALE", caf);
      assertPdfFieldEquals("PREVIOUS_STATE_4", "Illinois", caf);
    
    }
    
    @Test
    void shouldNotMapHHMemberLessThan5() throws Exception {
      fillInRequiredPages();
      selectPrograms("SNAP");
      fillOutHousemateInfoMoreThanFiveLessThanTen(3);
      var caf = submitAndDownloadCaf();
      assertNull(caf.getField("FIRST_NAME_4"));
    }
    
    @Test
    void shouldNotMapHHMemberMoreThan10() throws Exception {
      fillInRequiredPages();
      selectPrograms("SNAP");
      fillOutHousemateInfoMoreThanFiveLessThanTen(11);
      var caf = submitAndDownloadCaf();
      assertNull(caf.getField("FIRST_NAME_4"));
    }

    @Test
    void shouldMapMfipAsCash() throws Exception {
      fillInRequiredPages();
      selectPrograms("SNAP");
      postExpectingSuccess("applyForMFIP", "applyForMFIP", "true");

      var caf = submitAndDownloadCaf();

      assertPdfFieldEquals("FOOD", "Yes", caf);
      assertPdfFieldEquals("CASH", "Yes", caf);
    }

    @Test
    void shouldMarkCashIfUserSelectsBothCashAndMFIP() throws Exception {
      fillInRequiredPages();
      selectPrograms("CASH");
      postExpectingSuccess("applyForMFIP", "applyForMFIP", "true");

      var caf = submitAndDownloadCaf();

      assertPdfFieldEquals("CASH", "Yes", caf);
    }

    @Test
    void shouldMapNoforTemporarilyWithFriendsOrFamilyDueToEconomicHardship() throws Exception {
      fillInRequiredPages();
      postExpectingSuccess("livingSituation", "livingSituation",
          "TEMPORARILY_WITH_FRIENDS_OR_FAMILY_OTHER_REASONS");

      var caf = submitAndDownloadCaf();
      var ccap = downloadCcapClientPDF();

      assertPdfFieldEquals("LIVING_SITUATION", "TEMPORARILY_WITH_FRIENDS_OR_FAMILY", ccap);
      assertPdfFieldEquals("LIVING_SITUATION", "TEMPORARILY_WITH_FRIENDS_OR_FAMILY", caf);
      assertPdfFieldEquals("LIVING_WITH_FAMILY_OR_FRIENDS", "No", ccap);
    }

    @Test
    void shouldMapNoMedicalExpensesWhenNoneSelected() throws Exception {
      fillInRequiredPages();
      postExpectingSuccess("medicalExpenses", "medicalExpenses", List.of("NONE_OF_THE_ABOVE"));

      var caf = submitAndDownloadCaf();
      assertPdfFieldEquals("MEDICAL_EXPENSES_SELECTION", "NONE_SELECTED", caf);
    }

    @Test
    void shouldMapYesMedicalExpensesWhenOneSelected() throws Exception {
      fillInRequiredPages();
      postExpectingSuccess("medicalExpenses", "medicalExpenses",
          List.of("MEDICAL_INSURANCE_PREMIUMS"));

      var caf = submitAndDownloadCaf();
      assertPdfFieldEquals("MEDICAL_EXPENSES_SELECTION", "ONE_SELECTED", caf);
    }

    @Nested
    @Tag("pdf")
    class WithPersonalAndContactInfo {

      @BeforeEach
      void setUp() throws Exception {
        fillOutPersonalInfo();
        fillOutContactInfo();
      }

      @Test
      void shouldUseAdditionalIncomeInfoAsFutureIncomeWhenIncomeIs0() throws Exception {
        selectPrograms("CASH", Program.CCAP);
        postExpectingSuccess("addHouseholdMembers", "addHouseholdMembers", "false");
        postExpectingSuccess("employmentStatus", "areYouWorking", "false");
        postExpectingRedirect("unearnedIncome", "unearnedIncome", "NO_UNEARNED_INCOME_SELECTED",
            "otherUnearnedIncome");
        postExpectingRedirect("otherUnearnedIncome", "otherUnearnedIncome",
            "NO_OTHER_UNEARNED_INCOME_SELECTED", "futureIncome");

        var additionalIncomeInfo = "Here's something else about my situation";
        postExpectingRedirect("futureIncome", "additionalIncomeInfo", additionalIncomeInfo,
            "startExpenses");

        var caf = submitAndDownloadCaf();
        var ccap = downloadCcapClientPDF();
        List.of(caf, ccap).forEach(pdf -> {
          assertPdfFieldEquals("ADDITIONAL_INCOME_INFO", additionalIncomeInfo, pdf);
          assertPdfFieldEquals("ADDITIONAL_INCOME_INFO", additionalIncomeInfo, pdf);
        });
      }

      @Test
      void shouldMapOriginalHomeAddressToMailingAddressIfSameMailingAddressIsTrueAndUseEnrichedAddressIsFalse()
          throws Exception {
        String originalStreetAddress = "originalStreetAddress";
        String originalApt = "originalApt";
        String originalCity = "originalCity";
        String originalZipCode = "54321";
        postExpectingSuccess("homeAddress",
            Map.of("streetAddress", List.of(originalStreetAddress), "apartmentNumber",
                List.of(originalApt), "city", List.of(originalCity), "zipCode",
                List.of(originalZipCode), "state", List.of("MN")));
        postExpectingSuccess("mailingAddress", "sameMailingAddress", "true"); // THE KEY DIFFERENCE
        postExpectingSuccess("verifyHomeAddress", "useEnrichedAddress", "false");

        var ccap = submitAndDownloadCcap();
        assertPdfFieldEquals("APPLICANT_MAILING_STREET_ADDRESS", originalStreetAddress, ccap);
        assertPdfFieldEquals("APPLICANT_MAILING_CITY", originalCity, ccap);
        assertPdfFieldEquals("APPLICANT_MAILING_STATE", "MN", ccap);
        assertPdfFieldEquals("APPLICANT_MAILING_ZIPCODE", originalZipCode, ccap);
      }


      @Test
      void shouldMapCoverPageCountyInstructionsCorrectlyForCountiesThatUseTheGenericInstructions()
          throws Exception {
        postExpectingSuccess("identifyCounty", "county", "Morrison");
        testThatCorrectCountyInstructionsAreDisplayed("Little Falls", "56345",
            "This application was submitted to Morrison County with the information that you provided. Some parts of this application will be blank. A caseworker will follow up with you if additional information is needed.\n\nFor more support, you can call Morrison County (800-269-1464).");
      }

      @Test
      void shouldMapCoverPageCountyInstructionsCorrectlyForOtherCountiesThatUseTheGenericInstructions()
          throws Exception {
        postExpectingSuccess("identifyCounty", "county", "Dodge");
        testThatCorrectCountyInstructionsAreDisplayed("Dodge Center", "55927",
            "This application was submitted to Dodge County with the information that you provided. Some parts of this application will be blank. A caseworker will follow up with you if additional information is needed.\n\nFor more support, you can call Dodge County (507-923-2900).");
      }
      
      @Test
      void shouldMapCoverPageSelfEmploymentField()
          throws Exception {
        postExpectingSuccess("identifyCounty", "county", "Morrison");
        addFirstJob(getApplicantFullNameAndId(), "someEmployerName");
        addSelfEmployedJob(getApplicantFullNameAndId(), "My own boss");
        completeHelperWorkflow(true);
        var ccap = submitAndDownloadCcap();
        assertPdfFieldEquals("SELF_EMPLOYMENT_0", "No", ccap);
        assertPdfFieldEquals("SELF_EMPLOYMENT_1", "Yes", ccap);
      }

      @Test
      void shouldMapEnrichedHomeAddressToMailingAddressIfSameMailingAddressIsTrueAndUseEnrichedAddressIsTrue()
          throws Exception {
        String enrichedStreetValue = "testStreet";
        String enrichedCityValue = "testCity";
        String enrichedZipCodeValue = "testZipCode";
        String enrichedApartmentNumber = "someApt";
        String enrichedState = "someState";
        when(locationClient.validateAddress(any()))
            .thenReturn(Optional.of(new Address(enrichedStreetValue, enrichedCityValue,
                enrichedState, enrichedZipCodeValue, enrichedApartmentNumber, "Hennepin")));
        postExpectingSuccess("homeAddress",
            Map.of("streetAddress", List.of("originalStreetAddress"), "apartmentNumber",
                List.of("originalApt"), "city", List.of("originalCity"), "zipCode",
                List.of("54321"), "state", List.of("MN")));
        postExpectingSuccess("mailingAddress", "sameMailingAddress", "true"); // THE KEY DIFFERENCE
        postExpectingSuccess("verifyHomeAddress", "useEnrichedAddress", "true");

        var caf = submitAndDownloadCaf();
        var ccap = downloadCcapClientPDF();
        List.of(caf, ccap).forEach(pdf -> {
          assertPdfFieldEquals("APPLICANT_MAILING_STREET_ADDRESS", enrichedStreetValue, pdf);
          assertPdfFieldEquals("APPLICANT_MAILING_CITY", enrichedCityValue, pdf);
          assertPdfFieldEquals("APPLICANT_MAILING_STATE", enrichedState, pdf);
          assertPdfFieldEquals("APPLICANT_MAILING_ZIPCODE", enrichedZipCodeValue, pdf);
        });
        assertPdfFieldEquals("APPLICANT_MAILING_APT_NUMBER", enrichedApartmentNumber, caf);
      }

      @Test
      void shouldMapToOriginalMailingAddressIfSameMailingAddressIsFalseAndUseEnrichedAddressIsFalse()
          throws Exception {
        postExpectingSuccess("homeAddress",
            Map.of("isHomeless", List.of(""), "streetAddress", List.of("originalHomeStreetAddress"),
                "apartmentNumber", List.of("originalHomeApt"), "city", List.of("originalHomeCity"),
                "zipCode", List.of("54321"), "state", List.of("MN"), "sameMailingAddress",
                List.of("") // THE KEY DIFFERENCE
            ));
        String originalStreetAddress = "originalStreetAddress";
        String originalApt = "originalApt";
        String originalCity = "originalCity";
        String originalState = "IL";
        postExpectingSuccess("mailingAddress",
            Map.of("streetAddress", List.of(originalStreetAddress), "apartmentNumber",
                List.of(originalApt), "city", List.of(originalCity), "zipCode", List.of("54321"),
                "state", List.of(originalState), "sameMailingAddress", List.of("false") // THE KEY
                                                                                        // DIFFERENCE
            ));
        postExpectingSuccess("verifyMailingAddress", "useEnrichedAddress", "false");

        var caf = submitAndDownloadCaf();
        var ccap = downloadCcapClientPDF();
        List.of(caf, ccap).forEach(pdf -> {
          assertPdfFieldEquals("APPLICANT_MAILING_STREET_ADDRESS", originalStreetAddress, pdf);
          assertPdfFieldEquals("APPLICANT_MAILING_CITY", originalCity, pdf);
          assertPdfFieldEquals("APPLICANT_MAILING_STATE", originalState, pdf);
          assertPdfFieldEquals("APPLICANT_MAILING_ZIPCODE", "54321", pdf);
        });

        assertPdfFieldEquals("APPLICANT_MAILING_APT_NUMBER", originalApt, caf);
      }      
    }
    
   @Nested
   @Tag("pdf")
   class RaceAndEthinicityCAF{
     @Test
     void shouldMarkWhiteAndWriteToClientReportedFieldWithMiddleEasternOrNorthAfricanOnly() throws Exception {
       selectPrograms("SNAP");

       postExpectingSuccess("raceAndEthnicity", "raceAndEthnicity", "MIDDLE_EASTERN_OR_NORTH_AFRICAN");

       var caf = submitAndDownloadCaf();
       assertPdfFieldEquals("WHITE", "Yes", caf);
       assertPdfFieldEquals("CLIENT_REPORTED", "Middle Eastern / N. African", caf);
     }
     
     @Test
     void shouldMarkUnableToDetermineWithHispanicLatinoOrSpanishOnly() throws Exception {
       selectPrograms("SNAP");

       postExpectingSuccess("raceAndEthnicity", "raceAndEthnicity", "HISPANIC_LATINO_OR_SPANISH");

       var caf = submitAndDownloadCaf();
       assertPdfFieldEquals("HISPANIC_LATINO_OR_SPANISH", "Yes", caf);
       assertPdfFieldEquals("UNABLE_TO_DETERMINE", "Yes", caf);
     }
     
     @Test
     void shouldNotMarkUnableToDetermineWithHispanicLatinoOrSpanishAndAsianSelected() throws Exception {
       selectPrograms("SNAP");
      
       postExpectingSuccess("raceAndEthnicity",
           Map.of("raceAndEthnicity", List.of("ASIAN","HISPANIC_LATINO_OR_SPANISH","WHITE")
           ));
       var caf = submitAndDownloadCaf();
       assertPdfFieldEquals("ASIAN", "Yes", caf);
       assertPdfFieldEquals("WHITE", "Yes", caf);
       assertPdfFieldEquals("HISPANIC_LATINO_OR_SPANISH", "Yes", caf);
       assertPdfFieldEquals("UNABLE_TO_DETERMINE", "Off", caf);
     }
     
     @Test
     void shouldMarkWhiteWhenWhiteSelected() throws Exception {
       selectPrograms("SNAP");
      
       postExpectingSuccess("raceAndEthnicity",
           Map.of("raceAndEthnicity", List.of("ASIAN","WHITE","MIDDLE_EASTERN_OR_NORTH_AFRICAN")
           ));
       var caf = submitAndDownloadCaf();
       assertPdfFieldEquals("WHITE", "Yes", caf);
       assertPdfFieldEquals("ASIAN", "Yes", caf);
       assertPdfFieldEquals("HISPANIC_LATINO_OR_SPANISH", "Off", caf);
       assertPdfFieldEquals("UNABLE_TO_DETERMINE", "Off", caf);
       assertPdfFieldEquals("CLIENT_REPORTED", "", caf);
     }
     
     @Test
     void shouldWriteClientReportedWhenOtherRaceOrEthnicitySelected() throws Exception {
       selectPrograms("SNAP");
       postExpectingSuccess("raceAndEthnicity",
           Map.of("raceAndEthnicity", List.of("SOME_OTHER_RACE_OR_ETHNICITY","ASIAN"),
               "otherRaceOrEthnicity",List.of("SomeOtherRaceOrEthnicity")
           ));
       var caf = submitAndDownloadCaf();
       assertPdfFieldEquals("CLIENT_REPORTED", "SomeOtherRaceOrEthnicity", caf);
     }
     
     @Test
     void shouldWriteClientReportedForOthersOnlyWhenOtherRaceOrEthnicityAndMENASelected() throws Exception {
       selectPrograms("SNAP");
       postExpectingSuccess("raceAndEthnicity",
           Map.of("raceAndEthnicity", List.of("SOME_OTHER_RACE_OR_ETHNICITY","MIDDLE_EASTERN_OR_NORTH_AFRICAN"),
               "otherRaceOrEthnicity",List.of("SomeOtherRaceOrEthnicity")
           ));
       var caf = submitAndDownloadCaf();
       assertPdfFieldEquals("WHITE", "Off", caf);
       assertPdfFieldEquals("CLIENT_REPORTED", "SomeOtherRaceOrEthnicity", caf);
     }
  
 
     @Test
     void shouldWriteClientReportedForOthersWhenOtherRaceOrEthnicityAndWHITESelected()
         throws Exception {
       selectPrograms("SNAP");
       postExpectingSuccess("raceAndEthnicity",
           Map.of("raceAndEthnicity", List.of("SOME_OTHER_RACE_OR_ETHNICITY", "WHITE"),
               "otherRaceOrEthnicity", List.of("SomeOtherRaceOrEthnicity")));
       var caf = submitAndDownloadCaf();
       assertPdfFieldEquals("WHITE", "Yes", caf);
       assertPdfFieldEquals("CLIENT_REPORTED", "SomeOtherRaceOrEthnicity", caf);
     }
   }
   
   @Nested
   @Tag("pdf")
   class RaceAndEthinicityCCAP{
     @Test
     void shouldMarkWhiteAndWriteToClientReportedFieldWithMiddleEasternOrNorthAfricanOnly() throws Exception {
       selectPrograms("CCAP");

       postExpectingSuccess("raceAndEthnicity", "raceAndEthnicity", "MIDDLE_EASTERN_OR_NORTH_AFRICAN");

       var ccap = submitAndDownloadCcap();
       assertPdfFieldEquals("WHITE", "Yes", ccap);
       assertPdfFieldEquals("CLIENT_REPORTED", "Middle Eastern / N. African", ccap);
     }
     
     @Test
     void shouldMarkUnableToDetermineWithHispanicLatinoOrSpanishOnly() throws Exception {
       selectPrograms("CCAP");

       postExpectingSuccess("raceAndEthnicity", "raceAndEthnicity", "HISPANIC_LATINO_OR_SPANISH");

       var ccap = submitAndDownloadCcap();
       assertPdfFieldEquals("HISPANIC_LATINO_OR_SPANISH", "Yes", ccap);
       assertPdfFieldEquals("UNABLE_TO_DETERMINE", "Yes", ccap);
     }
     
     @Test
     void shouldNotMarkUnableToDetermineWithHispanicLatinoOrSpanishAndAsianSelected() throws Exception {
       selectPrograms("CCAP");
      
       postExpectingSuccess("raceAndEthnicity",
           Map.of("raceAndEthnicity", List.of("ASIAN","HISPANIC_LATINO_OR_SPANISH","WHITE")
           ));
       var ccap = submitAndDownloadCcap();
       assertPdfFieldEquals("ASIAN", "Yes", ccap);
       assertPdfFieldEquals("WHITE", "Yes", ccap);
       assertPdfFieldEquals("HISPANIC_LATINO_OR_SPANISH", "Yes", ccap);
       assertPdfFieldEquals("UNABLE_TO_DETERMINE", "Off", ccap);
     }
     
     @Test
     void shouldMarkWhiteWhenWhiteSelected() throws Exception {
       selectPrograms("CCAP");
      
       postExpectingSuccess("raceAndEthnicity",
           Map.of("raceAndEthnicity", List.of("ASIAN","WHITE","MIDDLE_EASTERN_OR_NORTH_AFRICAN")
           ));
       var ccap = submitAndDownloadCcap();
       assertPdfFieldEquals("WHITE", "Yes", ccap);
       assertPdfFieldEquals("ASIAN", "Yes", ccap);
       assertPdfFieldEquals("HISPANIC_LATINO_OR_SPANISH", "Off", ccap);
       assertPdfFieldEquals("UNABLE_TO_DETERMINE", "Off", ccap);
       assertPdfFieldEquals("CLIENT_REPORTED", "", ccap);
     }
     
     @Test
     void shouldWriteClientReportedWhenOtherRaceOrEthnicitySelected() throws Exception {
       selectPrograms("CCAP");
       postExpectingSuccess("raceAndEthnicity",
           Map.of("raceAndEthnicity", List.of("SOME_OTHER_RACE_OR_ETHNICITY","ASIAN"),
               "otherRaceOrEthnicity",List.of("SomeOtherRaceOrEthnicity")
           ));
       var ccap = submitAndDownloadCcap();
       assertPdfFieldEquals("CLIENT_REPORTED", "SomeOtherRaceOrEthnicity", ccap);
     }
     
     @Test
     void shouldWriteClientReportedForOthersOnlyWhenOtherRaceOrEthnicityAndMENASelected() throws Exception {
       selectPrograms("CCAP");
       postExpectingSuccess("raceAndEthnicity",
           Map.of("raceAndEthnicity", List.of("SOME_OTHER_RACE_OR_ETHNICITY","MIDDLE_EASTERN_OR_NORTH_AFRICAN"),
               "otherRaceOrEthnicity",List.of("SomeOtherRaceOrEthnicity")
           ));
       var ccap = submitAndDownloadCcap();
       assertPdfFieldEquals("WHITE", "Off", ccap);
       assertPdfFieldEquals("CLIENT_REPORTED", "SomeOtherRaceOrEthnicity", ccap);
     }
  
 
     @Test
     void shouldWriteClientReportedForOthersWhenOtherRaceOrEthnicityAndWHITESelected()
         throws Exception {
       selectPrograms("CCAP");
       postExpectingSuccess("raceAndEthnicity",
           Map.of("raceAndEthnicity", List.of("SOME_OTHER_RACE_OR_ETHNICITY", "WHITE"),
               "otherRaceOrEthnicity", List.of("SomeOtherRaceOrEthnicity")));
       var ccap = submitAndDownloadCcap();
       assertPdfFieldEquals("WHITE", "Yes", ccap);
       assertPdfFieldEquals("CLIENT_REPORTED", "SomeOtherRaceOrEthnicity", ccap);
     }
   }
   
  }

  @Nested
  @Tag("pdf")
  class CertainPops {

    @Test
    void allFieldsDoGetWrittenToPDF() throws Exception {
      fillInRequiredPages();
      postExpectingSuccess("identifyCountyBeforeApplying", "county", List.of("Anoka"));
      selectPrograms("CERTAIN_POPS");
      postExpectingRedirect("basicCriteria", "basicCriteria",
          List.of("SIXTY_FIVE_OR_OLDER", "BLIND", "HAVE_DISABILITY_SSA", "HAVE_DISABILITY_SMRT",
              "MEDICAL_ASSISTANCE", "SSI_OR_RSDI", "HELP_WITH_MEDICARE"),
          "certainPopsConfirm");

      fillInPersonalInfoAndContactInfoAndAddress();
      postExpectingSuccess("livingSituation", "livingSituation",
          "LIVING_IN_A_PLACE_NOT_MEANT_FOR_HOUSING");
      fillInApplicantHealthcareCoverageQuestionAsTrue();

      postExpectingSuccess("employmentStatus", "areYouWorking", "true");
      postExpectingSuccess("longTermCare", "doYouNeedLongTermCare", "true");
      postExpectingSuccess("pastInjury", "didYouHaveAPastInjury", "true");
      postExpectingSuccess("retroactiveCoverage", "retroactiveCoverageQuestion", "true");
      postExpectingSuccess("medicalInOtherState", "medicalInOtherState", "true");
      addFirstJob(getApplicantFullNameAndId(), "someEmployerName");
      addSelfEmployedJob(getApplicantFullNameAndId(), "My own boss");
      postExpectingSuccess("assets", "assets", List.of("VEHICLE", "STOCK_BOND", "LIFE_INSURANCE", "BURIAL_ACCOUNT", "OWNERSHIP_BUSINESS", "REAL_ESTATE"));
      assertNavigationRedirectsToCorrectNextPage("assets", "savings");
      completeHelperWorkflow(true);

      submitApplication();
      var pdf = downloadCertainPopsCaseWorkerPDF(applicationData.getId());

      // Assert that cover page is present
      assertPdfFieldEquals("PROGRAMS", "CERTAIN_POPS", pdf);
      assertPdfFieldEquals("APPLICATION_ID", applicationData.getId(), pdf);

      // Basic Criteria Questions
      assertPdfFieldEquals("BLIND", "Yes", pdf);
      assertPdfFieldEquals("BLIND_OR_HAS_DISABILITY", "Yes", pdf);
      assertPdfFieldEquals("HAS_PHYSICAL_MENTAL_HEALTH_CONDITION", "Yes", pdf);
      assertPdfFieldEquals("NEED_LONG_TERM_CARE", "Yes", pdf);
      assertPdfFieldEquals("HAD_A_PAST_ACCIDENT_OR_INJURY", "Yes", pdf);
      assertPdfFieldEquals("RETROACTIVE_COVERAGE_HELP", "Yes", pdf);
      assertPdfFieldEquals("MEDICAL_IN_OTHER_STATE", "Yes", pdf);

      // Section 1
      assertPdfFieldEquals("APPLICANT_LAST_NAME", "Schrute", pdf);
      assertPdfFieldEquals("APPLICANT_FIRST_NAME", "Dwight", pdf);
      assertPdfFieldEquals("DATE_OF_BIRTH", "01/12/1928", pdf);
      assertPdfFieldEquals("APPLICANT_SSN", "123456789", pdf);
      assertPdfFieldEquals("NO_SSN", "Yes", pdf);
      assertPdfFieldEquals("MARITAL_STATUS", "NEVER_MARRIED", pdf);
      assertPdfFieldEquals("APPLICANT_SPOKEN_LANGUAGE_PREFERENCE", "ENGLISH", pdf);
      assertPdfFieldEquals("NEED_INTERPRETER", "Yes", pdf);

      // Section 2
      assertPdfFieldEquals("APPLICANT_HOME_STREET_ADDRESS", "someStreetAddress", pdf);
      assertPdfFieldEquals("APPLICANT_HOME_CITY", "someCity", pdf);
      assertPdfFieldEquals("APPLICANT_HOME_STATE", "MN", pdf);
      assertPdfFieldEquals("APPLICANT_HOME_ZIPCODE", "12345", pdf);
      assertPdfFieldEquals("APPLICANT_MAILING_STREET_ADDRESS", "smarty street", pdf);
      assertPdfFieldEquals("APPLICANT_MAILING_CITY", "City", pdf);
      assertPdfFieldEquals("APPLICANT_MAILING_STATE", "CA", pdf);
      assertPdfFieldEquals("APPLICANT_MAILING_ZIPCODE", "03104", pdf);

      // Section 3
      assertPdfFieldEquals("APPLICANT_HOME_COUNTY", "", pdf);
      assertPdfFieldEquals("APPLICANT_MAILING_COUNTY", "someCounty", pdf);
      assertPdfFieldEquals("LIVING_SITUATION_COUNTY", "Anoka", pdf);
      assertPdfFieldEquals("LIVING_SITUATION", "LIVING_IN_A_PLACE_NOT_MEANT_FOR_HOUSING", pdf);
      assertPdfFieldEquals("APPLICANT_PHONE_NUMBER", "7234567890", pdf);

      // Section 7 & appendix B: Authorized Rep
      assertPdfFieldEquals("WANT_AUTHORIZED_REP", "Yes", pdf);
      assertPdfFieldEquals("AUTHORIZED_REP_NAME", "My Helpful Friend", pdf);
      assertPdfFieldEquals("AUTHORIZED_REP_ADDRESS", "helperStreetAddress", pdf);
      assertPdfFieldEquals("AUTHORIZED_REP_CITY", "helperCity", pdf);
      assertPdfFieldEquals("AUTHORIZED_REP_ZIP_CODE", "54321", pdf);
      assertPdfFieldEquals("AUTHORIZED_REP_PHONE_NUMBER", "7234561111", pdf);
      assertPdfFieldEquals("APPLICANT_SIGNATURE", "Human McPerson", pdf);
      assertPdfFieldEquals("CREATED_DATE", "2020-01-01", pdf);

      // Section 9
      assertPdfFieldEquals("SELF_EMPLOYED", "Yes", pdf);
      assertPdfFieldEquals("SELF_EMPLOYMENT_APPLICANT_NAME", "Dwight Schrute", pdf);
      assertPdfFieldEquals("SELF_EMPLOYMENT_GROSS_MONTHLY_INCOME_0", "480.00", pdf);
      assertPdfFieldEquals("SELF_EMPLOYMENT_GROSS_MONTHLY_INCOME_1", "", pdf);

      // Section 10
      assertPdfFieldEquals("IS_WORKING", "Yes", pdf);
      assertPdfFieldEquals("NON_SELF_EMPLOYMENT_EMPLOYEE_FULL_NAME_0", "Dwight Schrute", pdf);
      assertPdfFieldEquals("NON_SELF_EMPLOYMENT_EMPLOYERS_NAME_0", "someEmployerName", pdf);
      assertPdfFieldEquals("NON_SELF_EMPLOYMENT_PAY_FREQUENCY_0", "Every week", pdf);
      assertPdfFieldEquals("NON_SELF_EMPLOYMENT_HOURLY_WAGE_0", "", pdf);
      assertPdfFieldEquals("NON_SELF_EMPLOYMENT_HOURS_A_WEEK_0", "", pdf);

      //CertainPops Healthcare Coverage Question
      assertPdfFieldEquals("HAVE_HEALTHCARE_COVERAGE", "Yes", pdf);
      
      //Section 18
      assertPdfFieldEquals("VEHICLE_OWNER_FULL_NAME_0", "Dwight Schrute", pdf);
      
      //Section 15
      assertPdfFieldEquals("STOCK_OWNER_FULL_NAME_0", "Dwight Schrute", pdf);
      
      //Section 20
      assertPdfFieldEquals("LIFE_INSURANCE_OWNER_FULL_NAME_0", "Dwight Schrute", pdf);
      
      //Section 21
      assertPdfFieldEquals("BURIAL_ACCOUNT_OWNER_FULL_NAME_0", "Dwight Schrute", pdf);
      
      //Section 22
      assertPdfFieldEquals("BUSINESS_OWNERSHIP_OWNER_FULL_NAME_0", "Dwight Schrute", pdf);
      
      //Section 16
      assertPdfFieldEquals("REAL_ESTATE_OWNER_FULL_NAME_0", "Dwight Schrute", pdf);
    }
    
    @Test
    void shouldMapAssetsOwnersForHousehold() throws Exception {
      fillInRequiredPages();
      postExpectingSuccess("identifyCountyBeforeApplying", "county", List.of("Anoka"));
      selectPrograms("CERTAIN_POPS");
      postExpectingRedirect("basicCriteria", "basicCriteria",
          List.of("SIXTY_FIVE_OR_OLDER", "BLIND", "HAVE_DISABILITY_SSA", "HAVE_DISABILITY_SMRT",
              "MEDICAL_ASSISTANCE", "SSI_OR_RSDI", "HELP_WITH_MEDICARE"),
          "certainPopsConfirm");
      addHouseholdMembersWithProgram("CCAP");
      String jimHalpertId = getFirstHouseholdMemberId();
      /*
       * fillInPersonalInfoAndContactInfoAndAddress(); postExpectingRedirect("addHouseholdMembers",
       * "addHouseholdMembers", "true", "startHousehold"); fillOutHousemateInfo("SNAP");
       */
      postExpectingSuccess("livingSituation", "livingSituation",
          "LIVING_IN_A_PLACE_NOT_MEANT_FOR_HOUSING");
      fillInApplicantHealthcareCoverageQuestionAsTrue();
      
      postExpectingSuccess("assets", "assets", List.of("VEHICLE", "STOCK_BOND", "REAL_ESTATE"));
      assertNavigationRedirectsToCorrectNextPage("assets", "vehicleAssetSource");
      postExpectingRedirect("vehicleAssetSource", "vehicleAssetSource", List.of("Dwight Schrute applicant","Jim Halpert " + jimHalpertId),
          "stockAssetSource");
      postExpectingRedirect("stockAssetSource", "stockAssetSource", List.of("Dwight Schrute applicant","Jim Halpert " + jimHalpertId),
          "realEstateAssetSource");
      postExpectingRedirect("realEstateAssetSource", "realEstateAssetSource", List.of("Jim Halpert " + jimHalpertId),
          "savings");
      completeHelperWorkflow(true);

      submitApplication();
      var pdf = downloadCertainPopsCaseWorkerPDF(applicationData.getId());

      //Section 18
      assertPdfFieldEquals("VEHICLE_OWNER_FULL_NAME_0", "Dwight Schrute", pdf);
      assertPdfFieldEquals("VEHICLE_OWNER_FULL_NAME_1", "Jim Halpert", pdf);
      
      //Section 15
      assertPdfFieldEquals("STOCK_OWNER_FULL_NAME_0", "Dwight Schrute", pdf);
      assertPdfFieldEquals("STOCK_OWNER_FULL_NAME_1", "Jim Halpert", pdf);
      
      //Section 16
      assertPdfFieldEquals("REAL_ESTATE_OWNER_FULL_NAME_0", "Jim Halpert", pdf);
    }

    @Test
    void shouldNotMapCertainPopsIfBasicCriteriaIsNoneOfTheAbove() throws Exception {
      fillInRequiredPages();
      postExpectingSuccess("identifyCountyBeforeApplying", "county", List.of("Anoka"));
      selectPrograms("CERTAIN_POPS", "SNAP");
      postExpectingRedirect("basicCriteria", "basicCriteria", List.of("NONE"),
          "certainPopsOffboarding");

      submitApplication();
      var zippedFiles = downloadAllClientPDFs();
      var caf = submitAndDownloadCaf();

      assertThat(zippedFiles.stream().noneMatch(file -> getDocumentType(file).equals(CERTAIN_POPS))).isEqualTo(true);
      assertPdfFieldEquals("PROGRAMS", "SNAP", caf);
    }

    @Test
    void shouldMapZeroLiquidAssetsAmountAsNoSavings() throws Exception {
      fillInRequiredPages();
      selectPrograms("SNAP");
      postExpectingSuccess("savings", "haveSavings", "true");
      postExpectingSuccess("savingsAmount", "liquidAssets", "0");

      var caf = submitAndDownloadCaf();

      assertPdfFieldEquals("HAVE_SAVINGS", "No", caf);
      assertPdfFieldEquals("EXPEDITED_QUESTION_2", "0.00", caf);
    }
    
    @Test
    void shouldMapZeroLiquidAssetsAmountAsYesSavingsWhenLeftBlank() throws Exception {
      fillInRequiredPages();
      selectPrograms("SNAP");
      postExpectingSuccess("savings", "haveSavings", "true");
      postExpectingSuccess("savingsAmount", "liquidAssets", "");

      var caf = submitAndDownloadCaf();

      assertPdfFieldEquals("HAVE_SAVINGS", "Yes", caf);
      assertPdfFieldEquals("EXPEDITED_QUESTION_2", "0.00", caf);
    }

    @Test
    void shouldNotMapSavingsAmountWhenSavingsIsNo() throws Exception {
      fillInRequiredPages();
      selectPrograms("SNAP");
      postExpectingSuccess("savings", "haveSavings", "false");

      var caf = submitAndDownloadCaf();

      assertPdfFieldEquals("HAVE_SAVINGS", "No", caf);
      assertPdfFieldEquals("EXPEDITED_QUESTION_2", "0.00", caf);
    }
  }
}
