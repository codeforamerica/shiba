package org.codeforamerica.shiba.output.pdf;

import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.codeforamerica.shiba.AbstractBasePageTest;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.pages.SuccessPage;
import org.codeforamerica.shiba.pages.config.FeatureFlag;
import org.codeforamerica.shiba.pages.config.FeatureFlagConfiguration;
import org.codeforamerica.shiba.pages.enrichment.Address;
import org.codeforamerica.shiba.pages.enrichment.LocationClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.codeforamerica.shiba.output.Document.CAF;
import static org.codeforamerica.shiba.output.Document.CCAP;
import static org.codeforamerica.shiba.output.caf.CoverPageInputsMapper.CHILDCARE_WAITING_LIST_UTM_SOURCE;
import static org.codeforamerica.shiba.pages.YesNoAnswer.NO;
import static org.codeforamerica.shiba.pages.YesNoAnswer.YES;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Tag("pdf")
public class PdfIntegrationTest extends AbstractBasePageTest {
    @MockBean
    Clock clock;

    @MockBean
    LocationClient locationClient;

    @MockBean
    FeatureFlagConfiguration featureFlagConfiguration;

    @Override
    @BeforeEach
    protected void setUp() throws IOException {
        super.setUp();
        when(clock.instant()).thenReturn(Instant.now());
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
        when(locationClient.validateAddress(any())).thenReturn(Optional.empty());
        driver.navigate().to(baseUrl);
        testPage.clickButton("Apply now");
        testPage.clickContinue();
        testPage.clickContinue();
        navigateTo("addHouseholdMembers");
        testPage.clickButton("No");

        when(featureFlagConfiguration.get("submit-via-email")).thenReturn(FeatureFlag.OFF);
        when(featureFlagConfiguration.get("submit-via-api")).thenReturn(FeatureFlag.OFF);
        when(featureFlagConfiguration.get("county-anoka")).thenReturn(FeatureFlag.OFF);
    }

    @Nested
    @Tag("pdf")
    class CCAP {
        @BeforeEach
        void setUp() {
            selectPrograms(List.of(PROGRAM_CCAP));
        }

        @Test
        void shouldDefaultToNoForMillionDollarQuestionWhenQuestionPageIsNotShown() {
            navigateTo("energyAssistance");
            testPage.enter("energyAssistance", NO.getDisplayValue());
            testPage.enter("medicalExpenses", "None of the above");
            testPage.clickContinue();
            testPage.enter("supportAndCare", NO.getDisplayValue());
            testPage.enter("haveVehicle", NO.getDisplayValue());
            testPage.enter("ownRealEstate", NO.getDisplayValue());
            testPage.enter("haveInvestments", NO.getDisplayValue());
            testPage.enter("haveSavings", NO.getDisplayValue());

            Map<Document, PDAcroForm> pdAcroForms = submitAndDownloadReceipt();
            assertThat(pdAcroForms.get(CCAP).getField("HAVE_MILLION_DOLLARS").getValueAsString())
                    .isEqualTo("No");
        }

        @Test
        void shouldMarkYesForMillionDollarQuestionWhenChoiceIsYes() {
            navigateTo("energyAssistance");
            testPage.enter("energyAssistance", NO.getDisplayValue());
            testPage.enter("medicalExpenses", "None of the above");
            testPage.clickContinue();
            testPage.enter("supportAndCare", NO.getDisplayValue());
            testPage.enter("haveVehicle", NO.getDisplayValue());
            testPage.enter("ownRealEstate", NO.getDisplayValue());
            testPage.enter("haveInvestments", YES.getDisplayValue());
            testPage.enter("haveSavings", NO.getDisplayValue());
            testPage.enter("haveMillionDollars", YES.getDisplayValue());
            Map<Document, PDAcroForm> pdAcroForms = submitAndDownloadReceipt();
            assertThat(pdAcroForms.get(CCAP).getField("HAVE_MILLION_DOLLARS").getValueAsString())
                    .isEqualTo("Yes");
        }

        @Test
        void shouldMapAdultsInHouseholdRequestingChildcareAssistance() {
            addHouseholdMembers();

            navigateTo("childrenInNeedOfCare");
            testPage.enter("whoNeedsChildCare", "Jim Halpert");
            testPage.clickContinue();

            navigateTo("jobSearch");
            testPage.enter("currentlyLookingForJob", "Yes");
            testPage.enter("whoIsLookingForAJob", "Jim Halpert");
            testPage.enter("whoIsLookingForAJob", "Pam Beesly");
            testPage.clickContinue();

            navigateTo("whoIsGoingToSchool");
            testPage.enter("whoIsGoingToSchool", "Me");
            testPage.enter("whoIsGoingToSchool", "Jim Halpert");
            testPage.clickContinue();

            navigateTo("incomeByJob");
            testPage.clickButton("Add a job");
            testPage.enter("whoseJobIsIt", "Jim Halpert");
            testPage.clickContinue();
            testPage.enter("employersName", "Jim's Employer");
            testPage.clickContinue();
            testPage.enter("selfEmployment", "No");
            testPage.enter("paidByTheHour", "No");
            testPage.enter("payPeriod", "Every week");
            testPage.clickContinue();
            testPage.enter("incomePerPayPeriod", "1");
            testPage.clickContinue();

            testPage.clickButton("Add a job");
            testPage.enter("whoseJobIsIt", "Pam Beesly");
            testPage.clickContinue();
            testPage.enter("employersName", "Pam's Employer");
            testPage.clickContinue();
            testPage.enter("selfEmployment", "No");
            testPage.enter("paidByTheHour", "No");
            testPage.enter("payPeriod", "Every week");
            testPage.clickContinue();
            testPage.enter("incomePerPayPeriod", "1");
            testPage.clickContinue();

            Map<Document, PDAcroForm> pdAcroForms = submitAndDownloadReceipt();

            assertThat(getPdfFieldText(pdAcroForms.get(CCAP), "ADULT_REQUESTING_CHILDCARE_LOOKING_FOR_JOB_FULL_NAME_0"))
                    .isEqualTo("Pam Beesly");
            assertThat(getPdfFieldText(pdAcroForms.get(CCAP), "ADULT_REQUESTING_CHILDCARE_GOING_TO_SCHOOL_FULL_NAME_0"))
                    .isEqualTo("Dwight Schrute");
            assertThat(getPdfFieldText(pdAcroForms.get(CCAP),
                                       "ADULT_REQUESTING_CHILDCARE_WORKING_FULL_NAME_0")).isEqualTo("Pam Beesly");
            assertThat(getPdfFieldText(pdAcroForms.get(CCAP),
                                       "ADULT_REQUESTING_CHILDCARE_WORKING_EMPLOYERS_NAME_0")).isEqualTo(
                    "Pam's Employer");
            assertThat(getPdfFieldText(pdAcroForms.get(CCAP), "ADULT_REQUESTING_CHILDCARE_LOOKING_FOR_JOB_FULL_NAME_1"))
                    .isEmpty();
            assertThat(getPdfFieldText(pdAcroForms.get(CCAP), "ADULT_REQUESTING_CHILDCARE_GOING_TO_SCHOOL_FULL_NAME_1"))
                    .isEmpty();
            assertThat(getPdfFieldText(pdAcroForms.get(CCAP),
                                       "ADULT_REQUESTING_CHILDCARE_WORKING_FULL_NAME_1")).isEmpty();
            assertThat(getPdfFieldText(pdAcroForms.get(CCAP),
                                       "ADULT_REQUESTING_CHILDCARE_WORKING_EMPLOYERS_NAME_1")).isEmpty();
        }

        @Test
        void shouldNotMapUnearnedIncomeCcapWhenNoneOfTheAboveIsSelected() {
            fillInRequiredPages();
            navigateTo("unearnedIncomeCcap");
            testPage.enter("unearnedIncomeCcap", "None of the above");
            testPage.clickContinue();

            Map<Document, PDAcroForm> pdAcroForms = submitAndDownloadReceipt();
            PDAcroForm ccap = pdAcroForms.get(CCAP);
            assertThat(getPdfFieldText(ccap, "BENEFITS")).isEqualTo("No");
            assertThat(getPdfFieldText(ccap, "INSURANCE_PAYMENTS")).isEqualTo("No");
            assertThat(getPdfFieldText(ccap, "CONTRACT_FOR_DEED")).isEqualTo("No");
            assertThat(getPdfFieldText(ccap, "TRUST_MONEY")).isEqualTo("No");
            assertThat(getPdfFieldText(ccap, "HEALTH_CARE_REIMBURSEMENT")).isEqualTo("No");
            assertThat(getPdfFieldText(ccap, "INTEREST_DIVIDENDS")).isEqualTo("No");
            assertThat(getPdfFieldText(ccap, "OTHER_SOURCES")).isEqualTo("No");
        }

        @Test
        void shouldMapRecognizedUtmSource() {
            navigateTo("languagePreferences?utm_source=" + CHILDCARE_WAITING_LIST_UTM_SOURCE);
            fillInRequiredPages();

            Map<Document, PDAcroForm> pdAcroForms = submitAndDownloadReceipt();
            assertThat(getPdfFieldText(pdAcroForms.get(CCAP), "UTM_SOURCE")).isEqualTo("FROM BSF WAITING LIST");
        }
    }

    @Nested
    @Tag("pdf")
    class CAF {
        @BeforeEach
        void setUp() {
            selectPrograms(List.of(PROGRAM_CASH));
        }

        @Test
        void shouldMapJobLastThirtyDayIncomeAllBlankIsUndetermined() {
            addHouseholdMembers();
            fillInRequiredPages();

            navigateTo("incomeByJob");
            testPage.clickButton("Add a job");
            testPage.enter("whoseJobIsIt", "Jim Halpert");
            testPage.clickContinue();
            testPage.enter("employersName", "someEmployerName");
            testPage.clickContinue();
            testPage.enter("selfEmployment", "No");

            testPage.clickElementById("subtle-link");
            testPage.enter("lastThirtyDaysJobIncome", "");
            testPage.clickContinue();

            testPage.clickButton("Add a job");
            testPage.enter("whoseJobIsIt", "Me");
            testPage.clickContinue();
            testPage.enter("employersName", "someEmployerName");
            testPage.clickContinue();
            testPage.enter("selfEmployment", "No");

            testPage.clickElementById("subtle-link");
            testPage.enter("lastThirtyDaysJobIncome", "");
            testPage.clickContinue();

            PDAcroForm pdAcroForm = submitAndDownloadCaf();
            assertThat(getPdfFieldText(pdAcroForm, "SNAP_EXPEDITED_ELIGIBILITY")).isBlank();
        }

        @Test
        void shouldNotAddAuthorizedRepFieldsIfNo() {
            navigateTo("authorizedRep");
            testPage.enter("communicateOnYourBehalf", NO.getDisplayValue());

            PDAcroForm pdAcroForm = submitAndDownloadCaf();
            assertThat(getPdfFieldText(pdAcroForm, "AUTHORIZED_REP_FILL_OUT_FORM")).isEqualTo("Off");
            assertThat(getPdfFieldText(pdAcroForm, "AUTHORIZED_REP_GET_NOTICES")).isEqualTo("Off");
            assertThat(getPdfFieldText(pdAcroForm, "AUTHORIZED_REP_SPEND_ON_YOUR_BEHALF")).isEqualTo("Off");
        }

        @Test
        void shouldNotMapRecognizedUtmSource() {
            navigateTo("languagePreferences?utm_source=" + CHILDCARE_WAITING_LIST_UTM_SOURCE);
            assertThat(getPdfFieldText(submitAndDownloadCaf(), "UTM_SOURCE")).isEmpty();
        }
    }

    @Nested
    @Tag("pdf")
    class CAFandCCAP {
        @BeforeEach
        void setUp() {
            selectPrograms(List.of(PROGRAM_SNAP, PROGRAM_CCAP, PROGRAM_CASH));
        }

        @Test
        void shouldMapOriginalAddressIfHomeAddressDoesNotUseEnrichedAddress() {
            navigateTo("homeAddress");
            String originalStreetAddress = "originalStreetAddress";
            String originalApt = "originalApt";
            String originalCity = "originalCity";
            String originalZipCode = "54321";
            testPage.enter("streetAddress", originalStreetAddress);
            testPage.enter("apartmentNumber", originalApt);
            testPage.enter("city", originalCity);
            testPage.enter("zipCode", originalZipCode);
            testPage.enter("sameMailingAddress", "No, use a different address for mail");
            testPage.clickContinue();
            testPage.clickButton("Use this address");

            Map<Document, PDAcroForm> pdAcroForms = submitAndDownloadReceipt();
            PDAcroForm pdAcroForm = pdAcroForms.get(CCAP);
            assertThat(pdAcroForm.getField("APPLICANT_HOME_STREET_ADDRESS").getValueAsString())
                    .isEqualTo(originalStreetAddress);
            assertThat(pdAcroForm.getField("APPLICANT_HOME_CITY").getValueAsString())
                    .isEqualTo(originalCity);
            assertThat(pdAcroForm.getField("APPLICANT_HOME_STATE").getValueAsString())
                    .isEqualTo("MN");
            assertThat(pdAcroForm.getField("APPLICANT_HOME_ZIPCODE").getValueAsString())
                    .isEqualTo(originalZipCode);
        }

        @Test
        void shouldMapNoForSelfEmployment() {
            navigateTo("addHouseholdMembers");
            testPage.enter("addHouseholdMembers", NO.getDisplayValue());
            testPage.clickContinue();
            navigateTo("incomeByJob");
            testPage.clickButton("Add a job");
            testPage.enter("employersName", "someEmployerName");
            testPage.clickContinue();
            testPage.enter("selfEmployment", "No");
            testPage.enter("paidByTheHour", "No");
            testPage.enter("payPeriod", "Every week");
            testPage.clickContinue();
            testPage.enter("incomePerPayPeriod", "1");
            testPage.clickContinue();

            Map<Document, PDAcroForm> pdAcroForms = submitAndDownloadReceipt();
            assertThat(pdAcroForms.get(CAF).getField("SELF_EMPLOYED").getValueAsString()).isEqualTo("No");

            assertThat(pdAcroForms.get(CCAP)
                               .getField("NON_SELF_EMPLOYMENT_EMPLOYERS_NAME_0")
                               .getValueAsString()).isEqualTo("someEmployerName");
            assertThat(pdAcroForms.get(CCAP)
                               .getField("NON_SELF_EMPLOYMENT_PAY_FREQUENCY_0")
                               .getValueAsString()).isEqualTo("Every week");
            assertThat(pdAcroForms.get(CCAP)
                               .getField("NON_SELF_EMPLOYMENT_GROSS_MONTHLY_INCOME_0")
                               .getValueAsString()).isEqualTo("4.00");
        }

        @Test
        void shouldMapEnrichedAddressIfHomeAddressUsesEnrichedAddress() {
            navigateTo("homeAddress");
            testPage.enter("streetAddress", "originalStreetAddress");
            testPage.enter("apartmentNumber", "originalApt");
            testPage.enter("city", "originalCity");
            testPage.enter("zipCode", "54321");
            testPage.enter("sameMailingAddress", "No, use a different address for mail");
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
            testPage.clickContinue();
            testPage.clickContinue();
            Map<Document, PDAcroForm> pdAcroForms = submitAndDownloadReceipt();
            List.of(CAF, CCAP).forEach(type -> {
                PDAcroForm pdAcroForm = pdAcroForms.get(type);
                assertThat(pdAcroForm.getField("APPLICANT_HOME_STREET_ADDRESS").getValueAsString())
                        .isEqualTo(enrichedStreetValue);
                assertThat(pdAcroForm.getField("APPLICANT_HOME_CITY").getValueAsString())
                        .isEqualTo(enrichedCityValue);
                assertThat(pdAcroForm.getField("APPLICANT_HOME_STATE").getValueAsString())
                        .isEqualTo(enrichedState);
                assertThat(pdAcroForm.getField("APPLICANT_HOME_ZIPCODE").getValueAsString())
                        .isEqualTo(enrichedZipCodeValue);
            });

            assertThat(pdAcroForms.get(CAF).getField("APPLICANT_HOME_APT_NUMBER").getValueAsString())
                    .isEqualTo(enrichedApartmentNumber);
        }

        @Nested
        @Tag("pdf")
        class WithPersonalAndContactInfo {
            @BeforeEach
            void setUp() {
                fillOutPersonalInfo();
                testPage.clickContinue();
                fillOutContactInfo();
                testPage.clickContinue();
            }

            @Test
            void shouldMapOriginalHomeAddressToMailingAddressIfSameMailingAddressIsTrueAndUseEnrichedAddressIsFalse() {
                navigateTo("homeAddress");
                String originalStreetAddress = "originalStreetAddress";
                String originalApt = "originalApt";
                String originalCity = "originalCity";
                String originalZipCode = "54321";
                testPage.enter("streetAddress", originalStreetAddress);
                testPage.enter("apartmentNumber", originalApt);
                testPage.enter("city", originalCity);
                testPage.enter("zipCode", originalZipCode);
                testPage.enter("sameMailingAddress", "Yes, send mail here");
                testPage.clickContinue();
                testPage.clickButton("Use this address");
                Map<Document, PDAcroForm> pdAcroForms = submitAndDownloadReceipt();
                PDAcroForm pdAcroForm = pdAcroForms.get(CCAP);
                assertThat(pdAcroForm.getField("APPLICANT_MAILING_STREET_ADDRESS").getValueAsString())
                        .isEqualTo(originalStreetAddress);
                assertThat(pdAcroForm.getField("APPLICANT_MAILING_CITY").getValueAsString())
                        .isEqualTo(originalCity);
                assertThat(pdAcroForm.getField("APPLICANT_MAILING_STATE").getValueAsString())
                        .isEqualTo("MN");
                assertThat(pdAcroForm.getField("APPLICANT_MAILING_ZIPCODE").getValueAsString())
                        .isEqualTo(originalZipCode);
            }

            @Test
            void shouldMapDefaultCoverPageCountyInstructionsIfCountyIsFlaggedOff() {
                navigateTo("homeAddress");
                String originalStreetAddress = "2168 7th Ave";
                String originalCity = "Anoka";
                String originalZipCode = "55303";
                testPage.enter("streetAddress", originalStreetAddress);
                testPage.enter("city", originalCity);
                testPage.enter("zipCode", originalZipCode);
                testPage.enter("sameMailingAddress", "Yes, send mail here");
                testPage.clickContinue();
                testPage.clickButton("Use this address");
                Map<Document, PDAcroForm> pdAcroForms = submitAndDownloadReceipt();
                PDAcroForm pdAcroForm = pdAcroForms.get(CCAP);
                assertThat(pdAcroForm.getField("COUNTY_INSTRUCTIONS").getValueAsString())
                        .isEqualTo(
                                "This application was submitted. A caseworker at Hennepin County will help route your application to your county. For more support with your application, you can call Hennepin County at 612-596-1300.");
            }

            @Test
            void shouldMapEnrichedHomeAddressToMailingAddressIfSameMailingAddressIsTrueAndUseEnrichedAddressIsTrue() {
                navigateTo("homeAddress");
                testPage.enter("streetAddress", "originalStreetAddress");
                testPage.enter("apartmentNumber", "originalApt");
                testPage.enter("city", "originalCity");
                testPage.enter("zipCode", "54321");
                testPage.enter("sameMailingAddress", "Yes, send mail here");
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
                testPage.clickContinue();
                testPage.clickContinue();
                Map<Document, PDAcroForm> pdAcroForms = submitAndDownloadReceipt();
                List.of(CAF, CCAP).forEach(type -> {
                    PDAcroForm pdAcroForm = pdAcroForms.get(type);
                    assertThat(pdAcroForm.getField("APPLICANT_MAILING_STREET_ADDRESS").getValueAsString())
                            .isEqualTo(enrichedStreetValue);
                    assertThat(pdAcroForm.getField("APPLICANT_MAILING_CITY").getValueAsString())
                            .isEqualTo(enrichedCityValue);
                    assertThat(pdAcroForm.getField("APPLICANT_MAILING_STATE").getValueAsString())
                            .isEqualTo(enrichedState);
                    assertThat(pdAcroForm.getField("APPLICANT_MAILING_ZIPCODE").getValueAsString())
                            .isEqualTo(enrichedZipCodeValue);
                });
                assertThat(pdAcroForms.get(CAF).getField("APPLICANT_MAILING_APT_NUMBER").getValueAsString())
                        .isEqualTo(enrichedApartmentNumber);
            }

            @Test
            void shouldMapToOriginalMailingAddressIfSameMailingAddressIsFalseAndUseEnrichedAddressIsFalse() {
                navigateTo("homeAddress");
                fillInAddress();
                testPage.enter("sameMailingAddress", "No, use a different address for mail");
                testPage.clickContinue();
                testPage.clickButton("Use this address");
                String originalStreetAddress = "originalStreetAddress";
                String originalApt = "originalApt";
                String originalCity = "originalCity";
                String originalState = "IL";
                String originalZipCode = "54321";
                testPage.enter("streetAddress", originalStreetAddress);
                testPage.enter("apartmentNumber", originalApt);
                testPage.enter("city", originalCity);
                testPage.enter("state", originalState);
                testPage.enter("zipCode", originalZipCode);
                testPage.clickContinue();
                testPage.clickButton("Use this address");

                Map<Document, PDAcroForm> pdAcroForms = submitAndDownloadReceipt();
                List.of(CAF, CCAP).forEach(type -> {
                    PDAcroForm pdAcroForm = pdAcroForms.get(type);
                    assertThat(pdAcroForm.getField("APPLICANT_MAILING_STREET_ADDRESS").getValueAsString())
                            .isEqualTo(originalStreetAddress);
                    assertThat(pdAcroForm.getField("APPLICANT_MAILING_CITY").getValueAsString())
                            .isEqualTo(originalCity);
                    assertThat(pdAcroForm.getField("APPLICANT_MAILING_STATE").getValueAsString())
                            .isEqualTo(originalState);
                    assertThat(pdAcroForm.getField("APPLICANT_MAILING_ZIPCODE").getValueAsString())
                            .isEqualTo(originalZipCode);
                });

                assertThat(pdAcroForms.get(CAF).getField("APPLICANT_MAILING_APT_NUMBER").getValueAsString())
                        .isEqualTo(originalApt);
            }
        }

        @Test
        void shouldMapFullEmployeeNames() {
            addHouseholdMembers();

            navigateTo("incomeByJob");
            testPage.clickButton("Add a job");
            testPage.enter("whoseJobIsIt", "Jim Halpert");
            testPage.clickContinue();
            testPage.enter("employersName", "someEmployerName");
            testPage.clickContinue();
            testPage.enter("selfEmployment", "No");
            testPage.enter("paidByTheHour", "No");
            testPage.enter("payPeriod", "Every week");
            testPage.clickContinue();
            testPage.enter("incomePerPayPeriod", "1");
            testPage.clickContinue();

            Map<Document, PDAcroForm> pdAcroForms = submitAndDownloadReceipt();
            assertThat(getPdfFieldText(pdAcroForms.get(CAF), "EMPLOYEE_FULL_NAME_0"))
                    .isEqualTo("Jim Halpert");

            assertThat(pdAcroForms.get(CCAP).getField("NON_SELF_EMPLOYMENT_EMPLOYEE_FULL_NAME_0").getValueAsString())
                    .isEqualTo("Jim Halpert");

        }

        @Test
        void shouldMapJobLastThirtyDayIncome() {
            addHouseholdMembers();

            fillInRequiredPages();

            navigateTo("incomeByJob");
            testPage.clickButton("Add a job");
            testPage.enter("whoseJobIsIt", "Jim Halpert");
            testPage.clickContinue();
            testPage.enter("employersName", "someEmployerName");
            testPage.clickContinue();
            testPage.enter("selfEmployment", "No");

            testPage.clickElementById("subtle-link");
            testPage.enter("lastThirtyDaysJobIncome", "123");
            testPage.clickContinue();

            Map<Document, PDAcroForm> pdAcroForms = submitAndDownloadReceipt();
            assertThat(getPdfFieldText(pdAcroForms.get(CAF), "GROSS_MONTHLY_INCOME_0")).isEqualTo("123.00");
            assertThat(getPdfFieldText(pdAcroForms.get(CAF), "MONEY_MADE_LAST_MONTH")).isEqualTo("123.00");
            assertThat(getPdfFieldText(pdAcroForms.get(CAF), "SNAP_EXPEDITED_ELIGIBILITY")).isEqualTo("SNAP");

            assertThat(getPdfFieldText(pdAcroForms.get(CCAP), "NON_SELF_EMPLOYMENT_GROSS_MONTHLY_INCOME_0")).isEqualTo(
                    "123.00");
        }

        @Test
        void shouldMapJobLastThirtyDayIncomeSomeBlankIsDetermined() {
            addHouseholdMembers();

            fillInRequiredPages();

            navigateTo("incomeByJob");
            // Job 1
            testPage.clickButton("Add a job");
            testPage.enter("whoseJobIsIt", "Jim Halpert");
            testPage.clickContinue();
            testPage.enter("employersName", "someEmployerName");
            testPage.clickContinue();
            testPage.enter("selfEmployment", "No");

            testPage.clickElementById("subtle-link");
            testPage.enter("lastThirtyDaysJobIncome", "123");
            testPage.clickContinue();

            // Job 2
            testPage.clickButton("Add a job");
            testPage.enter("whoseJobIsIt", "Me");
            testPage.clickContinue();
            testPage.enter("employersName", "someEmployerName");
            testPage.clickContinue();
            testPage.enter("selfEmployment", "No");

            testPage.clickElementById("subtle-link");
            testPage.enter("lastThirtyDaysJobIncome", "");
            testPage.clickContinue();

            Map<Document, PDAcroForm> pdAcroForms = submitAndDownloadReceipt();
            assertThat(getPdfFieldText(pdAcroForms.get(CAF), "GROSS_MONTHLY_INCOME_0")).isEqualTo("123.00");
            assertThat(getPdfFieldText(pdAcroForms.get(CAF), "MONEY_MADE_LAST_MONTH")).isEqualTo("123.00");
            assertThat(getPdfFieldText(pdAcroForms.get(CAF), "SNAP_EXPEDITED_ELIGIBILITY")).isEqualTo("SNAP");

            assertThat(getPdfFieldText(pdAcroForms.get(CCAP), "NON_SELF_EMPLOYMENT_GROSS_MONTHLY_INCOME_0")).isEqualTo(
                    "123.00");
        }

        @Test
        void shouldMapLivingSituationToUnknownIfNoneOfTheseIsSelectedAndShouldNotMapTemporarilyWithFriendsOrFamilyYesNo() {
            fillInRequiredPages();

            navigateTo("livingSituation");
            testPage.enter("livingSituation", "None of these");
            testPage.clickContinue();

            Map<Document, PDAcroForm> pdAcroForms = submitAndDownloadReceipt();

            assertThat(getPdfFieldText(pdAcroForms.get(CAF), "LIVING_SITUATION")).isEqualTo("UNKNOWN");
            assertThat(getPdfFieldText(pdAcroForms.get(CCAP), "LIVING_SITUATION")).isEqualTo("UNKNOWN");
            assertThat(getPdfFieldText(pdAcroForms.get(CCAP), "LIVING_WITH_FAMILY_OR_FRIENDS")).isEqualTo("Off");
        }

        @Test
        void shouldMapLivingSituationToUnknownIfNotAnswered() {
            fillInRequiredPages();
            navigateTo("livingSituation");
            testPage.clickContinue();

            Map<Document, PDAcroForm> pdAcroForms = submitAndDownloadReceipt();

            assertThat(getPdfFieldText(pdAcroForms.get(CAF), "LIVING_SITUATION")).isEqualTo("UNKNOWN");
            assertThat(getPdfFieldText(pdAcroForms.get(CCAP), "LIVING_SITUATION")).isEqualTo("UNKNOWN");
        }

        @Test
        void shouldMapLivingWithFamilyAndFriendsDueToEconomicHardship() {
            fillInRequiredPages();
            navigateTo("livingSituation");
            testPage.enter("livingSituation",
                           "Temporarily staying with friends or family because I lost my housing or can no longer afford my own housing");
            testPage.clickContinue();

            Map<Document, PDAcroForm> pdAcroForms = submitAndDownloadReceipt();

            assertThat(getPdfFieldText(pdAcroForms.get(CCAP), "LIVING_SITUATION")).isEqualTo(
                    "TEMPORARILY_WITH_FRIENDS_OR_FAMILY");
            assertThat(getPdfFieldText(pdAcroForms.get(CAF), "LIVING_SITUATION")).isEqualTo(
                    "TEMPORARILY_WITH_FRIENDS_OR_FAMILY");
            assertThat(getPdfFieldText(pdAcroForms.get(CCAP), "LIVING_WITH_FAMILY_OR_FRIENDS")).isEqualTo("Yes");
        }

        @Test
        void ShouldMapNoforTemporarilyWithFriendsOrFamilyDueToEconomicHardship() {
            fillInRequiredPages();
            navigateTo("livingSituation");
            testPage.enter("livingSituation", "Temporarily staying with friends or family for other reasons");
            testPage.clickContinue();

            Map<Document, PDAcroForm> pdAcroForms = submitAndDownloadReceipt();

            assertThat(getPdfFieldText(pdAcroForms.get(CCAP), "LIVING_SITUATION")).isEqualTo(
                    "TEMPORARILY_WITH_FRIENDS_OR_FAMILY");
            assertThat(getPdfFieldText(pdAcroForms.get(CAF), "LIVING_SITUATION")).isEqualTo(
                    "TEMPORARILY_WITH_FRIENDS_OR_FAMILY");
            assertThat(getPdfFieldText(pdAcroForms.get(CCAP), "LIVING_WITH_FAMILY_OR_FRIENDS")).isEqualTo("No");
        }

        @Test
        void shouldMapNoMedicalExpensesWhenNoneSelected() {
            fillInRequiredPages();
            navigateTo("medicalExpenses");
            testPage.enter("medicalExpenses", "None of the above");
            testPage.clickContinue();

            Map<Document, PDAcroForm> pdAcroForms = submitAndDownloadReceipt();
            assertThat(getPdfFieldText(pdAcroForms.get(CAF), "MEDICAL_EXPENSES_SELECTION")).isEqualTo("NONE_SELECTED");
        }

        @Test
        void shouldMapYesMedicalExpensesWhenOneSelected() {
            fillInRequiredPages();
            navigateTo("medicalExpenses");
            testPage.enter("medicalExpenses", "Medical bills or copays");
            testPage.clickContinue();

            Map<Document, PDAcroForm> pdAcroForms = submitAndDownloadReceipt();
            assertThat(getPdfFieldText(pdAcroForms.get(CAF), "MEDICAL_EXPENSES_SELECTION")).isEqualTo("ONE_SELECTED");
        }
    }

    private void addHouseholdMembers() {
        navigateTo("personalInfo");
        testPage.enter("firstName", "Dwight");
        testPage.enter("lastName", "Schrute");
        testPage.clickContinue();

        navigateTo("addHouseholdMembers");
        testPage.enter("addHouseholdMembers", YES.getDisplayValue());
        testPage.clickContinue();

        testPage.enter("firstName", "Jim");
        testPage.enter("lastName", "Halpert");
        testPage.enter("programs", PROGRAM_CCAP);
        testPage.clickContinue();

        testPage.clickButton("Add a person");

        testPage.enter("firstName", "Pam");
        testPage.enter("lastName", "Beesly");
        testPage.enter("programs", PROGRAM_CCAP);
        testPage.clickContinue();
    }

    private void fillInRequiredPages() {
        navigateTo("migrantFarmWorker");
        testPage.enter("migrantOrSeasonalFarmWorker", NO.getDisplayValue());
        navigateTo("utilities");
        testPage.enter("payForUtilities", "Cooling");
        testPage.clickContinue();
    }

    private void fillInAddress() {
        testPage.enter("streetAddress", "originalHomeStreetAddress");
        testPage.enter("apartmentNumber", "originalHomeApt");
        testPage.enter("city", "originalHomeCity");
        testPage.enter("zipCode", "54321");
    }

    private Map<Document, PDAcroForm> submitAndDownloadReceipt() {
        navigateTo("signThisApplication");
        testPage.enter("applicantSignature", "someSignature");
        testPage.clickButton("Submit");
        if (driver.getPageSource().contains("Skip this for now")) {
            // Skip button might not be present if application does not support doc upload
            skipDocumentUploadFlow();
        }
        SuccessPage successPage = new SuccessPage(driver);
        successPage.downloadPdfs();
        await().until(() -> getAllFiles().size() == successPage.pdfDownloadLinks());

        return getAllFiles();
    }

    private PDAcroForm submitAndDownloadCaf() {
        return submitAndDownloadReceipt().get(CAF);
    }

    private void selectPrograms(List<String> programs) {
        navigateTo("choosePrograms");
        testPage.enter("programs", programs);
        testPage.clickContinue();
    }
}
