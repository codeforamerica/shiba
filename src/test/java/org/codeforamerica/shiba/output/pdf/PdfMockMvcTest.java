package org.codeforamerica.shiba.output.pdf;

import static org.codeforamerica.shiba.output.caf.CoverPageInputsMapper.CHILDCARE_WAITING_LIST_UTM_SOURCE;
import static org.codeforamerica.shiba.testutilities.TestUtils.assertPdfFieldEquals;
import static org.codeforamerica.shiba.testutilities.TestUtils.assertPdfFieldIsEmpty;
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
    postExpectingSuccess("languagePreferences", Map.of(
        "writtenLanguage", List.of("ENGLISH"),
        "spokenLanguage", List.of("ENGLISH"),
        "needInterpreter", List.of("true"))
    );

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
  void shouldMapChildrenNeedingChildcareFullNames() throws Exception {
    selectPrograms("CCAP");
    addHouseholdMembersWithCCAP();

    String jimHalpertId = getFirstHouseholdMemberId();
    postExpectingSuccess("childrenInNeedOfCare",
        "whoNeedsChildCare",
        List.of("Dwight Schrute applicant", "Jim Halpert " + jimHalpertId)
    );

    postExpectingSuccess("whoHasParentNotAtHome",
        "whoHasAParentNotLivingAtHome",
        List.of("Dwight Schrute applicant", "Jim Halpert " + jimHalpertId)
    );
    postExpectingSuccess("parentNotAtHomeNames", Map.of(
        "whatAreTheParentsNames", List.of("", "Jim's Parent"),
        "childIdMap", List.of("applicant", jimHalpertId)
    ));

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

    postExpectingSuccess("childrenInNeedOfCare",
        "whoNeedsChildCare",
        List.of("Dwight Schrute applicant", getJimFullNameAndId())
    );

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
    postExpectingSuccess("vehicle", "haveVehicle", "false");
    postExpectingSuccess("realEstate", "ownRealEstate", "false");
    postExpectingSuccess("investments", "haveInvestments", "false");
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
    postExpectingSuccess("vehicle", "haveVehicle", "false");
    postExpectingSuccess("realEstate", "ownRealEstate", "false");
    postExpectingSuccess("investments", "haveInvestments", "true");
    postExpectingSuccess("savings", "haveSavings", "false");
    postExpectingSuccess("millionDollar", "haveMillionDollars", "true");

    var ccap = submitAndDownloadCcap();
    assertPdfFieldEquals("HAVE_MILLION_DOLLARS", "Yes", ccap);
  }

  @Test
  void shouldNotMapUnearnedIncomeCcapWhenNoneOfTheAboveIsSelected() throws Exception {
    fillInRequiredPages();
    postExpectingSuccess("unearnedIncomeCcap", "unearnedIncomeCcap",
        "NO_UNEARNED_INCOME_CCAP_SELECTED");

    var ccap = submitAndDownloadCcap();
    assertPdfFieldEquals("BENEFITS", "No", ccap);
    assertPdfFieldEquals("INSURANCE_PAYMENTS", "No", ccap);
    assertPdfFieldEquals("CONTRACT_FOR_DEED", "No", ccap);
    assertPdfFieldEquals("TRUST_MONEY", "No", ccap);
    assertPdfFieldEquals("HEALTH_CARE_REIMBURSEMENT", "No", ccap);
    assertPdfFieldEquals("INTEREST_DIVIDENDS", "No", ccap);
    assertPdfFieldEquals("OTHER_SOURCES", "No", ccap);
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
    postExpectingSuccess("homeAddress", Map.of(
        "streetAddress", List.of("2168 7th Ave"),
        "city", List.of(city),
        "zipCode", List.of(zip),
        "state", List.of("MN"),
        "sameMailingAddress", List.of("true")
    ));
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
      postExpectingSuccess("homeAddress", Map.of(
          "streetAddress", List.of(originalStreetAddress),
          "apartmentNumber", List.of(originalApt),
          "city", List.of(originalCity),
          "zipCode", List.of(originalZipCode),
          "state", List.of("MN"),
          "sameMailingAddress", List.of("false")
      ));
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

      var ccap = downloadCcap();
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
          .thenReturn(Optional.of(new Address(
              enrichedStreetValue,
              enrichedCityValue,
              enrichedState,
              enrichedZipCodeValue,
              enrichedApartmentNumber,
              "Hennepin")));

      postExpectingSuccess("homeAddress", Map.of(
          "streetAddress", List.of("originalStreetAddress"),
          "apartmentNumber", List.of("originalApt"),
          "city", List.of("originalCity"),
          "zipCode", List.of("54321"),
          "state", List.of("MN"),
          "sameMailingAddress", List.of()
      ));

      postExpectingSuccess("verifyHomeAddress", "useEnrichedAddress", "true");

      var caf = submitAndDownloadCaf();
      var ccap = downloadCcap();

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
      var ccap = downloadCcap();

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
      var ccap = downloadCcap();

      assertPdfFieldEquals("GROSS_MONTHLY_INCOME_0", "123.00", caf);
      assertPdfFieldEquals("MONEY_MADE_LAST_MONTH", "123.00", caf);
      assertPdfFieldEquals("SNAP_EXPEDITED_ELIGIBILITY", "SNAP", caf);

      assertPdfFieldEquals("NON_SELF_EMPLOYMENT_GROSS_MONTHLY_INCOME_0", "123.00", ccap);
    }

    @Test
    void shouldMapLivingSituationToUnknownIfNoneOfTheseIsSelectedAndShouldNotMapTemporarilyWithFriendsOrFamilyYesNo()
        throws
        Exception {
      fillInRequiredPages();

      postExpectingSuccess("livingSituation", "livingSituation", "UNKNOWN");

      var caf = submitAndDownloadCaf();
      var ccap = downloadCcap();

      assertPdfFieldEquals("LIVING_SITUATION", "UNKNOWN", caf);
      assertPdfFieldEquals("LIVING_SITUATION", "UNKNOWN", ccap);
      assertPdfFieldEquals("LIVING_WITH_FAMILY_OR_FRIENDS", "Off", ccap);
    }

    @Test
    void shouldMapLivingSituationToUnknownIfNotAnswered() throws Exception {
      fillInRequiredPages();
      postWithoutData("livingSituation");

      var caf = submitAndDownloadCaf();
      var ccap = downloadCcap();

      assertPdfFieldEquals("LIVING_SITUATION", "UNKNOWN", caf);
      assertPdfFieldEquals("LIVING_SITUATION", "UNKNOWN", ccap);
    }

    @Test
    void shouldMapLivingWithFamilyAndFriendsDueToEconomicHardship() throws Exception {
      fillInRequiredPages();
      postExpectingSuccess("livingSituation",
          "livingSituation",
          "TEMPORARILY_WITH_FRIENDS_OR_FAMILY_DUE_TO_ECONOMIC_HARDSHIP");

      var caf = submitAndDownloadCaf();
      var ccap = downloadCcap();

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
      var ccap = downloadCcap();

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
        selectPrograms(Program.CCAP);
        postExpectingSuccess("addHouseholdMembers", "addHouseholdMembers", "false");
        postExpectingSuccess("employmentStatus", "areYouWorking", "false");
        postExpectingRedirect("unearnedIncome",
            "unearnedIncome",
            "NO_UNEARNED_INCOME_SELECTED",
            "unearnedIncomeCcap");
        postExpectingRedirect("unearnedIncomeCcap",
            "unearnedIncomeCcap",
            "NO_UNEARNED_INCOME_CCAP_SELECTED",
            "additionalIncomeInfo");

        var additionalIncomeInfo = "Here's something else about my situation";
        postExpectingRedirect("additionalIncomeInfo", "additionalIncomeInfo",
            additionalIncomeInfo, "startExpenses");

        var caf = submitAndDownloadCaf();
        var ccap = downloadCcap();
        List.of(caf, ccap).forEach(pdf -> {
          assertPdfFieldEquals("ADDITIONAL_INCOME_INFO", additionalIncomeInfo, pdf);
          assertPdfFieldEquals("ADDITIONAL_INCOME_INFO", additionalIncomeInfo, pdf);
        });
      }

      @Test
      void shouldMapOriginalHomeAddressToMailingAddressIfSameMailingAddressIsTrueAndUseEnrichedAddressIsFalse()
          throws
          Exception {
        String originalStreetAddress = "originalStreetAddress";
        String originalApt = "originalApt";
        String originalCity = "originalCity";
        String originalZipCode = "54321";
        postExpectingSuccess("homeAddress", Map.of(
            "streetAddress", List.of(originalStreetAddress),
            "apartmentNumber", List.of(originalApt),
            "city", List.of(originalCity),
            "zipCode", List.of(originalZipCode),
            "state", List.of("MN")
        ));
        postExpectingSuccess("mailingAddress", "sameMailingAddress", "true"); // THE KEY DIFFERENCE
        postExpectingSuccess("verifyHomeAddress", "useEnrichedAddress", "false");

        var ccap = submitAndDownloadCcap();
        assertPdfFieldEquals("APPLICANT_MAILING_STREET_ADDRESS", originalStreetAddress, ccap);
        assertPdfFieldEquals("APPLICANT_MAILING_CITY", originalCity, ccap);
        assertPdfFieldEquals("APPLICANT_MAILING_STATE", "MN", ccap);
        assertPdfFieldEquals("APPLICANT_MAILING_ZIPCODE", originalZipCode, ccap);
      }

      @Test
      void shouldMapDefaultCoverPageCountyInstructionsIfCountyIsFlaggedOff() throws Exception {
        testThatCorrectCountyInstructionsAreDisplayed("Anoka", "55303",
            "This application was submitted. A caseworker at Hennepin County will help route your application to your county. Some parts of this application will be blank. A county worker will follow up with you if additional information is needed. For more support with your application, you can call Hennepin County at 612-596-1300.");
      }

      @Test
      void shouldMapCoverPageCountyInstructionsCorrectlyForHennepin() throws Exception {
        testThatCorrectCountyInstructionsAreDisplayed("Minneapolis", "55401",
            "This application was submitted to Hennepin County with the information that you provided. Some parts of this application will be blank. A county worker will follow up with you if additional information is needed.\n\nFor more support, you can call Hennepin County at 612-596-1300.");
      }

      @Test
      void shouldMapCoverPageCountyInstructionsCorrectlyForOlmsted() throws Exception {
        testThatCorrectCountyInstructionsAreDisplayed("Oronoco", "55960",
            "This application was submitted to Olmsted County with the information that you provided. Some parts of this application will be blank. A county worker will follow up with you if additional information is needed.\n\nFor more support, you can call Olmsted County at 507-328-6500.");
      }

      @Test
      void shouldMapCoverPageCountyInstructionsCorrectlyForWabasha() throws Exception {
        testThatCorrectCountyInstructionsAreDisplayed("Wabasha", "55981",
            "We have received your application that you submitted through MNbenefits.mn.gov. Processing your application in a timely fashion is our highest priority. We have submitted your application to an Eligibility Worker who will contact you no later than the end of the next business day to discuss your application. For questions or concerns, please contact us at 651.565.3351(collect calls accepted), toll free at 1.888.315.8815 or email us at imuinterview@co.wabasha.mn.us. Some parts of this application will be blank. A county worker will follow up with you if additional information is needed. Thank you and have a great day!");
      }

      @Test
      void shouldMapCoverPageCountyInstructionsCorrectlyForWright() throws Exception {
        testThatCorrectCountyInstructionsAreDisplayed("Waverly", "55390",
            "This application was submitted to Wright County with the information that you provided. Some parts of this application will be blank. A county worker will follow up with you if additional information is needed.\n\nFor more support, you can call Wright County at 763-682-7414.");
      }

      @Test
      void shouldMapCoverPageCountyInstructionsCorrectlyForCountiesThatUseTheGenericInstructions()
          throws Exception {
        testThatCorrectCountyInstructionsAreDisplayed("Little Falls", "56345",
            "This application was submitted to Morrison County with the information that you provided. Some parts of this application will be blank. A county worker will follow up with you if additional information is needed.\n\nFor more support, you can call Morrison County (320-631-3599).");
      }

      @Test
      void shouldMapCoverPageCountyInstructionsCorrectlyForOtherCountiesThatUseTheGenericInstructions()
          throws Exception {
        testThatCorrectCountyInstructionsAreDisplayed("Dodge Center", "55927",
            "This application was submitted to Dodge County with the information that you provided. Some parts of this application will be blank. A county worker will follow up with you if additional information is needed.\n\nFor more support, you can call Dodge County (507-431-5600).");
      }

      @Test
      void shouldMapEnrichedHomeAddressToMailingAddressIfSameMailingAddressIsTrueAndUseEnrichedAddressIsTrue()
          throws
          Exception {
        String enrichedStreetValue = "testStreet";
        String enrichedCityValue = "testCity";
        String enrichedZipCodeValue = "testZipCode";
        String enrichedApartmentNumber = "someApt";
        String enrichedState = "someState";
        when(locationClient.validateAddress(any()))
            .thenReturn(Optional.of(new Address(
                enrichedStreetValue,
                enrichedCityValue,
                enrichedState,
                enrichedZipCodeValue,
                enrichedApartmentNumber,
                "Hennepin")));
        postExpectingSuccess("homeAddress", Map.of(
            "streetAddress", List.of("originalStreetAddress"),
            "apartmentNumber", List.of("originalApt"),
            "city", List.of("originalCity"),
            "zipCode", List.of("54321"),
            "state", List.of("MN")
        ));
        postExpectingSuccess("mailingAddress", "sameMailingAddress", "true"); // THE KEY DIFFERENCE
        postExpectingSuccess("verifyHomeAddress", "useEnrichedAddress", "true");

        var caf = submitAndDownloadCaf();
        var ccap = downloadCcap();
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
          throws
          Exception {
        postExpectingSuccess("homeAddress", Map.of(
            "isHomeless", List.of(""),
            "streetAddress", List.of("originalHomeStreetAddress"),
            "apartmentNumber", List.of("originalHomeApt"),
            "city", List.of("originalHomeCity"),
            "zipCode", List.of("54321"),
            "state", List.of("MN"),
            "sameMailingAddress", List.of("") // THE KEY DIFFERENCE
        ));
        String originalStreetAddress = "originalStreetAddress";
        String originalApt = "originalApt";
        String originalCity = "originalCity";
        String originalState = "IL";
        postExpectingSuccess("mailingAddress", Map.of(
            "streetAddress", List.of(originalStreetAddress),
            "apartmentNumber", List.of(originalApt),
            "city", List.of(originalCity),
            "zipCode", List.of("54321"),
            "state", List.of(originalState),
            "sameMailingAddress", List.of("false") // THE KEY DIFFERENCE
        ));
        postExpectingSuccess("verifyMailingAddress", "useEnrichedAddress", "false");

        var caf = submitAndDownloadCaf();
        var ccap = downloadCcap();
        List.of(caf, ccap).forEach(pdf -> {
          assertPdfFieldEquals("APPLICANT_MAILING_STREET_ADDRESS", originalStreetAddress, pdf);
          assertPdfFieldEquals("APPLICANT_MAILING_CITY", originalCity, pdf);
          assertPdfFieldEquals("APPLICANT_MAILING_STATE", originalState, pdf);
          assertPdfFieldEquals("APPLICANT_MAILING_ZIPCODE", "54321", pdf);
        });

        assertPdfFieldEquals("APPLICANT_MAILING_APT_NUMBER", originalApt, caf);
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
      postExpectingRedirect("basicCriteria",
          "basicCriteria",
          List.of("SIXTY_FIVE_OR_OLDER", "BLIND", "HAVE_DISABILITY_SSA", "HAVE_DISABILITY_SMRT",
              "MEDICAL_ASSISTANCE", "SSI_OR_RSDI", "HELP_WITH_MEDICARE"),
          "introBasicInfo");

      fillInPersonalInfoAndContactInfoAndAddress();
      postExpectingSuccess("livingSituation", "livingSituation",
          "PAYING_FOR_HOUSING_WITH_RENT_LEASE_OR_MORTGAGE");

      completeHelperWorkflow(true);

      submitApplication();
      var pdf = downloadCertainPops(applicationData.getId());

      // Assert that cover page is present
      assertPdfFieldEquals("PROGRAMS", "CERTAIN_POPS", pdf);
      assertPdfFieldEquals("APPLICATION_ID", applicationData.getId(), pdf);

      // Basic Criteria Questions
      assertPdfFieldEquals("BLIND", "Yes", pdf);
      assertPdfFieldEquals("HELP_WITH_MEDICARE", "Yes", pdf);
      assertPdfFieldEquals("BLIND_OR_HAS_DISABILITY", "Yes", pdf);
      assertPdfFieldEquals("HAS_PHYSICAL_MENTAL_HEALTH_CONDITION", "Yes", pdf);

      // Section 1
      assertPdfFieldEquals("APPLICANT_LAST_NAME", "defaultLastName", pdf);
      assertPdfFieldEquals("APPLICANT_FIRST_NAME", "defaultFirstName", pdf);
      assertPdfFieldEquals("DATE_OF_BIRTH", "01/12/1928", pdf);
      assertPdfFieldEquals("APPLICANT_SSN", "123456789", pdf);
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
      //TODO home and mailing counties

      // Section 3
      assertPdfFieldEquals("APPLICANT_HOME_COUNTY", "", pdf);
      assertPdfFieldEquals("APPLICANT_MAILING_COUNTY", "someCounty", pdf);
      assertPdfFieldEquals("LIVING_SITUATION_COUNTY", "Anoka", pdf);
      assertPdfFieldEquals("LIVING_SITUATION", "PAYING_FOR_HOUSING_WITH_RENT_LEASE_OR_MORTGAGE",
          pdf);
      assertPdfFieldEquals("APPLICANT_PHONE_NUMBER", "7234567890", pdf);
      // TODO plan to make MN your home

      // Section 7 & appendix B: Authorized Rep
      assertPdfFieldEquals("WANT_AUTHORIZED_REP", "Yes", pdf);
      assertPdfFieldEquals("AUTHORIZED_REP_NAME", "My Helpful Friend", pdf);
      assertPdfFieldEquals("AUTHORIZED_REP_ADDRESS", "helperStreetAddress", pdf);
      assertPdfFieldEquals("AUTHORIZED_REP_CITY", "helperCity", pdf);
      assertPdfFieldEquals("AUTHORIZED_REP_ZIP_CODE", "54321", pdf);
      assertPdfFieldEquals("AUTHORIZED_REP_PHONE_NUMBER", "7234561111", pdf);
      assertPdfFieldEquals("APPLICANT_SIGNATURE", "Human McPerson", pdf);
      assertPdfFieldEquals("CREATED_DATE", "2020-01-01", pdf);
    }
  }
}
