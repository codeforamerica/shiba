package org.codeforamerica.shiba.output.pdf;

import org.codeforamerica.shiba.AbstractShibaMockMvcTest;
import org.codeforamerica.shiba.pages.enrichment.Address;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.codeforamerica.shiba.TestUtils.assertPdfFieldEquals;
import static org.codeforamerica.shiba.TestUtils.assertPdfFieldIsEmpty;
import static org.codeforamerica.shiba.output.caf.CoverPageInputsMapper.CHILDCARE_WAITING_LIST_UTM_SOURCE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@Tag("pdf")
public class PdfMockMvcTest extends AbstractShibaMockMvcTest {
    @Override
    @BeforeEach
    protected void setUp() throws Exception {
        super.setUp();
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

        @Test
        void shouldMapJobLastThirtyDayIncomeSomeBlankIsDetermined() throws Exception {
            addHouseholdMembers();

            fillInRequiredPages();

            // Add a job for Jim
            postWithQueryParam("incomeByJob", "option", "0");
            String jim = getJimFullNameAndId();
            postWithData("householdSelectionForIncome", "whoseJobIsIt", jim);
            postWithData("employersName", "employersName", "someEmployerName");
            postWithData("selfEmployment", "selfEmployment", "false");
            postWithData("lastThirtyDaysJobIncome", "lastThirtyDaysJobIncome", "123");

            // Add a job for Dwight
            postWithQueryParam("incomeByJob", "option", "0");
            String me = getApplicantFullNameAndId();
            postWithData("householdSelectionForIncome", "whoseJobIsIt", me);
            postWithData("employersName", "employersName", "someEmployerName");
            postWithData("selfEmployment", "selfEmployment", "false");
            postWithData("lastThirtyDaysJobIncome", "lastThirtyDaysJobIncome", "");

            var caf = submitAndDownloadCaf();
            var ccap = downloadCcap();

            assertPdfFieldEquals("GROSS_MONTHLY_INCOME_0", "123.00", caf);
            assertPdfFieldEquals("MONEY_MADE_LAST_MONTH", "123.00", caf);
            assertPdfFieldEquals("SNAP_EXPEDITED_ELIGIBILITY", "SNAP", caf);

            assertPdfFieldEquals("NON_SELF_EMPLOYMENT_GROSS_MONTHLY_INCOME_0", "123.00", ccap);
        }

        @Test
        void shouldMapLivingSituationToUnknownIfNoneOfTheseIsSelectedAndShouldNotMapTemporarilyWithFriendsOrFamilyYesNo() throws
                Exception {
            fillInRequiredPages();

            postWithData("livingSituation", "livingSituation", "UNKNOWN");


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
            postWithData("livingSituation",
                         "livingSituation",
                         "TEMPORARILY_WITH_FRIENDS_OR_FAMILY_DUE_TO_ECONOMIC_HARDSHIP");

            var caf = submitAndDownloadCaf();
            var ccap = downloadCcap();

            assertPdfFieldEquals("LIVING_SITUATION", "TEMPORARILY_WITH_FRIENDS_OR_FAMILY", ccap);
            assertPdfFieldEquals("LIVING_SITUATION", "TEMPORARILY_WITH_FRIENDS_OR_FAMILY", caf);
            assertPdfFieldEquals("LIVING_WITH_FAMILY_OR_FRIENDS", "Yes", ccap);
        }

        @Test
        void shouldMapNoforTemporarilyWithFriendsOrFamilyDueToEconomicHardship() throws Exception {
            fillInRequiredPages();
            postWithData("livingSituation", "livingSituation", "TEMPORARILY_WITH_FRIENDS_OR_FAMILY_OTHER_REASONS");

            var caf = submitAndDownloadCaf();
            var ccap = downloadCcap();

            assertPdfFieldEquals("LIVING_SITUATION", "TEMPORARILY_WITH_FRIENDS_OR_FAMILY", ccap);
            assertPdfFieldEquals("LIVING_SITUATION", "TEMPORARILY_WITH_FRIENDS_OR_FAMILY", caf);
            assertPdfFieldEquals("LIVING_WITH_FAMILY_OR_FRIENDS", "No", ccap);
        }

        @Test
        void shouldMapNoMedicalExpensesWhenNoneSelected() throws Exception {
            fillInRequiredPages();
            postWithData("medicalExpenses", "medicalExpenses", List.of("NONE_OF_THE_ABOVE"));

            var caf = submitAndDownloadCaf();
            assertPdfFieldEquals("MEDICAL_EXPENSES_SELECTION", "NONE_SELECTED", caf);
        }

        @Test
        void shouldMapYesMedicalExpensesWhenOneSelected() throws Exception {
            fillInRequiredPages();
            postWithData("medicalExpenses", "medicalExpenses", List.of("MEDICAL_INSURANCE_PREMIUMS"));

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
}
