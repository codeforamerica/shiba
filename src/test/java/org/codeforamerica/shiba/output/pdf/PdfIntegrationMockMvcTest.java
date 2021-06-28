package org.codeforamerica.shiba.output.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.codeforamerica.shiba.pages.config.FeatureFlag;
import org.codeforamerica.shiba.pages.config.PageTemplate;
import org.codeforamerica.shiba.pages.config.ReferenceOptionsTemplate;
import org.codeforamerica.shiba.pages.enrichment.Address;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.servlet.ModelAndView;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.*;

import static java.util.stream.Collectors.toMap;
import static org.codeforamerica.shiba.TestUtils.assertPdfFieldEquals;
import static org.codeforamerica.shiba.TestUtils.assertPdfFieldIsEmpty;
import static org.codeforamerica.shiba.output.caf.CoverPageInputsMapper.CHILDCARE_WAITING_LIST_UTM_SOURCE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class PdfIntegrationMockMvcTest extends AbstractPdfIntegrationMockMvcTest {
    @Autowired
    private MockMvc mockMvc;

    private MockHttpSession session;

    @BeforeEach
    void setUp() throws Exception {
        session = new MockHttpSession();

        when(clock.instant()).thenReturn(Instant.now());
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
        when(locationClient.validateAddress(any())).thenReturn(Optional.empty());
        when(featureFlagConfiguration.get("submit-via-email")).thenReturn(FeatureFlag.OFF);
        when(featureFlagConfiguration.get("submit-via-api")).thenReturn(FeatureFlag.OFF);
        when(featureFlagConfiguration.get("county-anoka")).thenReturn(FeatureFlag.OFF);

        mockMvc.perform(get("/pages/languagePreferences").session(session)); // start timer
        postWithData("languagePreferences", Map.of(
                "writtenLanguage", List.of("ENGLISH"),
                "spokenLanguage", List.of("ENGLISH"))
        );

        postWithData("addHouseholdMembers", "addHouseholdMembers", "false");
    }

    @Test
    void shouldAnswerEnergyAssistanceQuestion() throws Exception {
        selectPrograms("CASH");

        postWithData("energyAssistance", "energyAssistance", "true");
        postWithData("energyAssistanceMoreThan20", "energyAssistanceMoreThan20", "false");

        var caf = submitAndDownloadCaf();
        assertPdfFieldEquals("RECEIVED_LIHEAP", "No", caf);
    }

    @Test
    void shouldMapEnergyAssistanceWhenUserReceivedNoAssistance() throws Exception {
        selectPrograms("CASH");

        postWithData("energyAssistance", "energyAssistance", "false");

        var caf = submitAndDownloadCaf();
        assertPdfFieldEquals("RECEIVED_LIHEAP", "No", caf);
    }

    @Test
    void shouldMapChildrenNeedingChildcareFullNames() throws Exception {
        selectPrograms("CCAP");
        addHouseholdMembers();

        String jimHalpertId = getFirstHouseholdMemberId();
        postWithData("childrenInNeedOfCare",
                     "whoNeedsChildCare",
                     List.of("Dwight Schrute applicant", "Jim Halpert " + jimHalpertId)
        );

        postWithData("whoHasParentNotAtHome",
                     "whoHasAParentNotLivingAtHome",
                     List.of("Dwight Schrute applicant", "Jim Halpert " + jimHalpertId)
        );
        postWithData("parentNotAtHomeNames", Map.of(
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

        addHouseholdMembers();

        postWithData("childrenInNeedOfCare",
                     "whoNeedsChildCare",
                     List.of("Dwight Schrute applicant", getJimFullNameAndId())
        );

        postWithData("whoHasParentNotAtHome", "whoHasAParentNotLivingAtHome", "NONE_OF_THE_ABOVE");

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

        postWithData("energyAssistance", "energyAssistance", "false");
        postWithData("medicalExpenses", "medicalExpenses", "NONE_OF_THE_ABOVE");
        postWithData("supportAndCare", "supportAndCare", "false");
        postWithData("vehicle", "haveVehicle", "false");
        postWithData("realEstate", "ownRealEstate", "false");
        postWithData("investments", "haveInvestments", "false");
        postWithData("savings", "haveSavings", "false");

        var ccap = submitAndDownloadCcap();
        assertPdfFieldEquals("HAVE_MILLION_DOLLARS", "No", ccap);
    }

    @Test
    void shouldMarkYesForMillionDollarQuestionWhenChoiceIsYes() throws Exception {
        selectPrograms("CCAP");

        postWithData("energyAssistance", "energyAssistance", "false");
        postWithData("medicalExpenses", "medicalExpenses", "NONE_OF_THE_ABOVE");
        postWithData("supportAndCare", "supportAndCare", "false");
        postWithData("vehicle", "haveVehicle", "false");
        postWithData("realEstate", "ownRealEstate", "false");
        postWithData("investments", "haveInvestments", "true");
        postWithData("savings", "haveSavings", "false");
        postWithData("millionDollar", "haveMillionDollars", "true");

        var ccap = submitAndDownloadCcap();
        assertPdfFieldEquals("HAVE_MILLION_DOLLARS", "Yes", ccap);
    }

    @Test
    void shouldNotMapUnearnedIncomeCcapWhenNoneOfTheAboveIsSelected() throws Exception {
        fillInRequiredPages();
        postWithData("unearnedIncomeCcap", "unearnedIncomeCcap", "NO_UNEARNED_INCOME_CCAP_SELECTED");

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

        addHouseholdMembers();

        String jim = getJimFullNameAndId();
        postWithData("childrenInNeedOfCare", "whoNeedsChildCare", jim);

        postWithData("jobSearch", "currentlyLookingForJob", "true");
        String pam = getPamFullNameAndId();
        postWithData("whoIsLookingForAJob", "whoIsLookingForAJob", List.of(jim, pam));

        String me = getApplicantFullNameAndId();
        postWithData("whoIsGoingToSchool", "whoIsGoingToSchool", List.of(me, jim));

        // Add a job for Jim
        addFirstJob(jim, "Jim's Employer");

        // Add a job for Pam
        postWithQueryParam("jobBuilder", "option", "0");
        addJob(pam, "Pam's Employer");

        var ccap = submitAndDownloadCcap();
        assertPdfFieldEquals("ADULT_REQUESTING_CHILDCARE_LOOKING_FOR_JOB_FULL_NAME_0", "Pam Beesly", ccap);
        assertPdfFieldEquals("ADULT_REQUESTING_CHILDCARE_GOING_TO_SCHOOL_FULL_NAME_0", "Dwight Schrute", ccap);
        assertPdfFieldEquals("ADULT_REQUESTING_CHILDCARE_WORKING_FULL_NAME_0", "Pam Beesly", ccap);
        assertPdfFieldEquals("ADULT_REQUESTING_CHILDCARE_WORKING_EMPLOYERS_NAME_0", "Pam's Employer", ccap);
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
        addHouseholdMembers();
        fillInRequiredPages();

        // Add a job for Jim
        postWithQueryParam("incomeByJob", "option", "0");
        String jim = getJimFullNameAndId();
        postWithData("householdSelectionForIncome", "whoseJobIsIt", jim);
        postWithData("employersName", "employersName", "someEmployerName");
        postWithData("selfEmployment", "selfEmployment", "false");
        postWithData("lastThirtyDaysJobIncome", "lastThirtyDaysJobIncome", "");

        // Add a job for Dwight
        postWithQueryParam("incomeByJob", "option", "0");
        String me = getApplicantFullNameAndId();
        postWithData("householdSelectionForIncome", "whoseJobIsIt", me);
        postWithData("employersName", "employersName", "someEmployerName");
        postWithData("selfEmployment", "selfEmployment", "false");
        postWithData("lastThirtyDaysJobIncome", "lastThirtyDaysJobIncome", "");

        var caf = submitAndDownloadCaf();
        assertPdfFieldIsEmpty("SNAP_EXPEDITED_ELIGIBILITY", caf);
    }

    @Test
    void shouldNotAddAuthorizedRepFieldsIfNo() throws Exception {
        selectPrograms("CASH");
        postWithData("authorizedRep", "communicateOnYourBehalf", "false");

        var caf = submitAndDownloadCaf();
        assertPdfFieldEquals("AUTHORIZED_REP_FILL_OUT_FORM", "Off", caf);
        assertPdfFieldEquals("AUTHORIZED_REP_GET_NOTICES", "Off", caf);
        assertPdfFieldEquals("AUTHORIZED_REP_SPEND_ON_YOUR_BEHALF", "Off", caf);
    }

    @Test
    void shouldMapRecognizedUtmSourceCCAP() throws Exception {
        selectPrograms("CCAP");
        getWithQueryParam("languagePreferences", "utm_source", CHILDCARE_WAITING_LIST_UTM_SOURCE);
        fillInRequiredPages();

        var ccap = submitAndDownloadCcap();
        assertPdfFieldEquals("UTM_SOURCE", "FROM BSF WAITING LIST", ccap);
    }

    @Test
    void shouldNotMapRecognizedUtmSourceCAF() throws Exception {
        selectPrograms("CASH");
        getWithQueryParam("languagePreferences", "utm_source", CHILDCARE_WAITING_LIST_UTM_SOURCE);
        var caf = submitAndDownloadCaf();
        assertPdfFieldIsEmpty("UTM_SOURCE", caf);
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
            postWithData("homeAddress", Map.of(
                    "streetAddress", List.of(originalStreetAddress),
                    "apartmentNumber", List.of(originalApt),
                    "city", List.of(originalCity),
                    "zipCode", List.of(originalZipCode),
                    "state", List.of("MN"),
                    "sameMailingAddress", List.of("false")
            ));
            postWithData("verifyHomeAddress", "useEnrichedAddress", "false");

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

            postWithData("homeAddress", Map.of(
                    "streetAddress", List.of("originalStreetAddress"),
                    "apartmentNumber", List.of("originalApt"),
                    "city", List.of("originalCity"),
                    "zipCode", List.of("54321"),
                    "state", List.of("MN"),
                    "sameMailingAddress", List.of("false")
            ));

            postWithData("verifyHomeAddress", "useEnrichedAddress", "true");

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
            addHouseholdMembers();
            String jim = getJimFullNameAndId();
            addFirstJob(jim, "someEmployerName");

            var caf = submitAndDownloadCaf();
            var ccap = downloadCcap();

            assertPdfFieldEquals("EMPLOYEE_FULL_NAME_0", "Jim Halpert", caf);
            assertPdfFieldEquals("NON_SELF_EMPLOYMENT_EMPLOYEE_FULL_NAME_0", "Jim Halpert", ccap);
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
            void shouldMapOriginalHomeAddressToMailingAddressIfSameMailingAddressIsTrueAndUseEnrichedAddressIsFalse() throws
                    Exception {
                String originalStreetAddress = "originalStreetAddress";
                String originalApt = "originalApt";
                String originalCity = "originalCity";
                String originalZipCode = "54321";
                postWithData("homeAddress", Map.of(
                        "streetAddress", List.of(originalStreetAddress),
                        "apartmentNumber", List.of(originalApt),
                        "city", List.of(originalCity),
                        "zipCode", List.of(originalZipCode),
                        "state", List.of("MN"),
                        "sameMailingAddress", List.of("true") // THE KEY DIFFERENCE
                ));
                postWithData("verifyHomeAddress", "useEnrichedAddress", "false");

                var ccap = submitAndDownloadCcap();
                assertPdfFieldEquals("APPLICANT_MAILING_STREET_ADDRESS", originalStreetAddress, ccap);
                assertPdfFieldEquals("APPLICANT_MAILING_CITY", originalCity, ccap);
                assertPdfFieldEquals("APPLICANT_MAILING_STATE", "MN", ccap);
                assertPdfFieldEquals("APPLICANT_MAILING_ZIPCODE", originalZipCode, ccap);
            }

            @Test
            void shouldMapDefaultCoverPageCountyInstructionsIfCountyIsFlaggedOff() throws Exception {
                String originalStreetAddress = "2168 7th Ave";
                String originalCity = "Anoka";
                String originalZipCode = "55303";
                postWithData("homeAddress", Map.of(
                        "streetAddress", List.of(originalStreetAddress),
                        "city", List.of(originalCity),
                        "zipCode", List.of(originalZipCode),
                        "state", List.of("MN"),
                        "sameMailingAddress", List.of("true") // THE KEY DIFFERENCE
                ));
                postWithData("verifyHomeAddress", "useEnrichedAddress", "false");

                var ccap = submitAndDownloadCcap();
                assertPdfFieldEquals("COUNTY_INSTRUCTIONS",
                                     "This application was submitted. A caseworker at Hennepin County will help route your application to your county. For more support with your application, you can call Hennepin County at 612-596-1300.",
                                     ccap);
            }

            @Test
            void shouldMapEnrichedHomeAddressToMailingAddressIfSameMailingAddressIsTrueAndUseEnrichedAddressIsTrue() throws
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
                postWithData("homeAddress", Map.of(
                        "streetAddress", List.of("originalStreetAddress"),
                        "apartmentNumber", List.of("originalApt"),
                        "city", List.of("originalCity"),
                        "zipCode", List.of("54321"),
                        "state", List.of("MN"),
                        "sameMailingAddress", List.of("true") // THE KEY DIFFERENCE
                ));
                postWithData("verifyHomeAddress", "useEnrichedAddress", "true");

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
            void shouldMapToOriginalMailingAddressIfSameMailingAddressIsFalseAndUseEnrichedAddressIsFalse() throws
                    Exception {
                postWithData("homeAddress", Map.of(
                        "streetAddress", List.of("originalHomeStreetAddress"),
                        "apartmentNumber", List.of("originalHomeApt"),
                        "city", List.of("originalHomeCity"),
                        "zipCode", List.of("54321"),
                        "state", List.of("MN"),
                        "sameMailingAddress", List.of("false") // THE KEY DIFFERENCE
                ));
                postWithData("verifyHomeAddress", "useEnrichedAddress", "false");
                String originalStreetAddress = "originalStreetAddress";
                String originalApt = "originalApt";
                String originalCity = "originalCity";
                String originalState = "IL";
                postWithData("mailingAddress", Map.of(
                        "streetAddress", List.of(originalStreetAddress),
                        "apartmentNumber", List.of(originalApt),
                        "city", List.of(originalCity),
                        "zipCode", List.of("54321"),
                        "state", List.of(originalState)
                ));
                postWithData("verifyMailingAddress", "useEnrichedAddress", "false");

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

    @NotNull
    private String getApplicantFullNameAndId() {
        return "Dwight Schrute applicant";
    }

    @NotNull
    private String getPamFullNameAndId() throws Exception {
        return "Pam Beesly " + getSecondHouseholdMemberId();
    }

    @NotNull
    private String getJimFullNameAndId() throws Exception {
        return "Jim Halpert " + getFirstHouseholdMemberId();
    }

    private void addJob(String householdMemberFullNameAndId, String employersName) throws Exception {
        postWithData("householdSelectionForIncome", "whoseJobIsIt", householdMemberFullNameAndId);
        postWithData("employersName", "employersName", employersName);
        postWithData("selfEmployment", "selfEmployment", "false");
        postWithData("paidByTheHour", "paidByTheHour", "false");
        postWithData("payPeriod", "payPeriod", "EVERY_WEEK");
        postWithData("incomePerPayPeriod", "incomePerPayPeriod", "1");
    }

    private void postWithQueryParam(String pageName, String queryParam, String value) throws Exception {
        mockMvc.perform(
                post("/pages/" + pageName).session(session).with(csrf()).queryParam(queryParam, value)
        ).andExpect(redirectedUrl("/pages/" + pageName + "/navigation"));
    }

    private void getWithQueryParam(String pageName, String queryParam, String value) throws Exception {
        mockMvc.perform(
                get("/pages/" + pageName).session(session).queryParam(queryParam, value)
        ).andExpect(status().isOk());
    }

    private void addHouseholdMembers() throws Exception {
        postWithData("personalInfo", Map.of(
                "firstName", List.of("Dwight"),
                "lastName", List.of("Schrute"),
                "dateOfBirth", List.of("01", "12", "1928")
        ));
        postWithData("addHouseholdMembers", "addHouseholdMembers", "true");
        postWithData("householdMemberInfo", Map.of(
                "firstName", List.of("Jim"),
                "lastName", List.of("Halpert"),
                "programs", List.of("CCAP")
        ));
        postWithData("householdMemberInfo", Map.of(
                "firstName", List.of("Pam"),
                "lastName", List.of("Beesly"),
                "programs", List.of("CCAP")
        ));
    }

    private String getFirstHouseholdMemberId() throws Exception {
        return getHouseholdMemberIdAtIndex(0);
    }

    private String getSecondHouseholdMemberId() throws Exception {
        return getHouseholdMemberIdAtIndex(1);
    }

    private String getHouseholdMemberIdAtIndex(int index) throws Exception {
        ModelAndView modelAndView = Objects.requireNonNull(
                mockMvc.perform(get("/pages/childrenInNeedOfCare").session(session)).andReturn().getModelAndView());
        PageTemplate pageTemplate = (PageTemplate) modelAndView.getModel().get("page");
        ReferenceOptionsTemplate options = (ReferenceOptionsTemplate) pageTemplate.getInputs().get(0).getOptions();
        return options.getSubworkflows().get("household").get(index).getId().toString();
    }

    private PDAcroForm submitAndDownloadCaf() throws Exception {
        submitApplication();
        return downloadCaf();
    }

    private PDAcroForm submitAndDownloadCcap() throws Exception {
        submitApplication();
        return downloadCcap();
    }

    private PDAcroForm downloadCaf() throws Exception {
        var cafBytes = mockMvc.perform(get("/download").session(session))
                .andReturn()
                .getResponse()
                .getContentAsByteArray();
        return PDDocument.load(cafBytes).getDocumentCatalog().getAcroForm();
    }

    private PDAcroForm downloadCcap() throws Exception {
        var ccapBytes = mockMvc.perform(get("/download-ccap").session(session))
                .andReturn()
                .getResponse()
                .getContentAsByteArray();
        return PDDocument.load(ccapBytes).getDocumentCatalog().getAcroForm();
    }

    private void fillInRequiredPages() throws Exception {
        postWithData("migrantFarmWorker", "migrantOrSeasonalFarmWorker", "false");
        postWithData("utilities", "payForUtilities", "COOLING");
    }

    private void fillOutPersonalInfo() throws Exception {
        postWithData("personalInfo", Map.of(
                "firstName", List.of("defaultFirstName"),
                "lastName", List.of("defaultLastName"),
                "otherName", List.of("defaultOtherName"),
                "dateOfBirth", List.of("01", "12", "1928"),
                "ssn", List.of("123456789"),
                "maritalStatus", List.of("NEVER_MARRIED"),
                "sex", List.of("FEMALE"),
                "livedInMnWholeLife", List.of("true"),
                "moveToMnDate", List.of("02", "18", "1776"),
                "moveToMnPreviousCity", List.of("Chicago")
        ));
    }

    private void fillOutContactInfo() throws Exception {
        postWithData("contactInfo", Map.of(
                "phoneNumber", List.of("7234567890"),
                "phoneOrEmail", List.of("TEXT")
        ));
    }

    private void submitApplication() throws Exception {
        postWithData("/submit",
                     "/pages/signThisApplication/navigation",
                     Map.of("applicantSignature", List.of("Human McPerson")));
    }

    private void selectPrograms(String... programs) throws Exception {
        postWithData("choosePrograms", "programs", Arrays.stream(programs).toList());
    }

    // Post to a page with an arbitrary number of multi-value inputs
    private void postWithData(String pageName, Map<String, List<String>> params) throws Exception {
        String postUrl = getUrlForPageName(pageName);
        postWithData(postUrl, postUrl + "/navigation", params);
    }

    // Post to a page with a single input that only accepts a single value
    private void postWithData(String pageName, String inputName, String value) throws Exception {
        String postUrl = getUrlForPageName(pageName);
        var params = Map.of(inputName, List.of(value));
        postWithData(postUrl, postUrl + "/navigation", params);
    }

    // Post to a page with a single input that accepts multiple values
    private void postWithData(String pageName, String inputName, List<String> values) throws Exception {
        String postUrl = getUrlForPageName(pageName);
        postWithData(postUrl, postUrl + "/navigation", Map.of(inputName, values));
    }

    private void postWithData(String postUrl, String redirectUrl, Map<String, List<String>> params) throws Exception {
        Map<String, List<String>> paramsWithProperInputNames = params.entrySet().stream()
                .collect(toMap(e -> e.getKey() + "[]", Map.Entry::getValue));
        mockMvc.perform(
                post(postUrl)
                        .session(session)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .params(new LinkedMultiValueMap<>(paramsWithProperInputNames))
        ).andExpect(redirectedUrl(redirectUrl));
    }

    private String getUrlForPageName(String pageName) {
        return "/pages/" + pageName;
    }
}
