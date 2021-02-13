package org.codeforamerica.shiba.output.pdf;

import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.codeforamerica.shiba.AbstractBasePageTest;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.pages.SuccessPage;
import org.codeforamerica.shiba.pages.YesNoAnswer;
import org.codeforamerica.shiba.pages.config.FeatureFlag;
import org.codeforamerica.shiba.pages.config.FeatureFlagConfiguration;
import org.codeforamerica.shiba.pages.enrichment.Address;
import org.codeforamerica.shiba.pages.enrichment.LocationClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openqa.selenium.By;
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
import static org.codeforamerica.shiba.pages.YesNoAnswer.NO;
import static org.codeforamerica.shiba.pages.YesNoAnswer.YES;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Tag("integration")
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
        navigateTo("doYouLiveAlone");
        testPage.clickButton("Yes");

        when(featureFlagConfiguration.get("document-upload-feature")).thenReturn(FeatureFlag.ON);
        when(featureFlagConfiguration.get("submit-via-email")).thenReturn(FeatureFlag.OFF);
        when(featureFlagConfiguration.get("submit-via-api")).thenReturn(FeatureFlag.OFF);
        when(featureFlagConfiguration.get("send-non-partner-county-alert")).thenReturn(FeatureFlag.OFF);

    }

    @Nested
    class EnergyAssistanceLIHEAP {
        @BeforeEach
        void setUp() {
            selectPrograms(List.of("Cash programs"));
        }

        @ParameterizedTest
        @CsvSource(value = {
                "Yes,No,No",
                "Yes,Yes,Yes"
        })
        void shouldAnswerEnergyAssistanceQuestion(
                String hasEnergyAssistance,
                String hasMoreThan20ForEnergyAssistance,
                String result
        ) {
            navigateTo("energyAssistance");
            testPage.enter("energyAssistance", hasEnergyAssistance);
            testPage.enter("energyAssistanceMoreThan20", hasMoreThan20ForEnergyAssistance);

            PDAcroForm pdAcroForm = submitAndDownloadCaf();
            assertThat(pdAcroForm.getField("RECEIVED_LIHEAP").getValueAsString()).isEqualTo(result);
        }

        @Test
        void shouldMapEnergyAssistanceWhenUserReceivedNoAssistance() {
            navigateTo("energyAssistance");
            testPage.enter("energyAssistance", "No");

            PDAcroForm pdAcroForm = submitAndDownloadCaf();
            assertThat(pdAcroForm.getField("RECEIVED_LIHEAP").getValueAsString()).isEqualTo("No");
        }
    }

    @Nested
    class CCAP {
        @BeforeEach
        void setUp() {
            selectPrograms(List.of("Child Care Assistance"));
        }

        @Test
        void shouldMapChildrenNeedingChildcareFullNames() {
            addHouseholdMembers();

            testPage.clickButton("Yes, that's everyone");
            navigateTo("childrenInNeedOfCare");
            testPage.enter("whoNeedsChildCare", "Me");
            testPage.enter("whoNeedsChildCare", "Jim Halpert");
            testPage.clickContinue();

            Map<Document, PDAcroForm> pdAcroForms = submitAndDownloadReceipt();
            assertThat(pdAcroForms.get(CCAP).getField("CHILD_NEEDS_CHILDCARE_FULL_NAME_0").getValueAsString())
                    .isEqualTo("Dwight Schrute");
            assertThat(pdAcroForms.get(CCAP).getField("CHILD_NEEDS_CHILDCARE_FULL_NAME_1").getValueAsString())
                    .isEqualTo("Jim Halpert");
        }

        @Test
        void shouldMapStudentFullNames() {
            addHouseholdMembers();

            testPage.clickButton("Yes, that's everyone");
            navigateTo("goingToSchool");
            testPage.clickButton(YES.getDisplayValue());
            testPage.enter("whoIsGoingToSchool", "Me");
            testPage.enter("whoIsGoingToSchool", "Jim Halpert");
            testPage.clickContinue();
            navigateTo("childrenInNeedOfCare");
            testPage.enter("whoNeedsChildCare", "Jim Halpert");
            testPage.clickContinue();

            Map<Document, PDAcroForm> pdAcroForms = submitAndDownloadReceipt();
            assertThat(pdAcroForms.get(CCAP).getField("STUDENT_FULL_NAME_0").getValueAsString())
                    .isEqualTo("Jim Halpert");
        }

        @Test
        void shouldDefaultToNoForMillionDollarQuestionWhenQuestionPageIsNotShown() {
            navigateTo("energyAssistance");
            testPage.enter("energyAssistance", NO.getDisplayValue());
            testPage.enter("supportAndCare", NO.getDisplayValue());
            testPage.enter("haveVehicle", NO.getDisplayValue());
            testPage.enter("ownRealEstate", "No, I do not own any real estate");
            testPage.clickContinue();
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
            testPage.enter("supportAndCare", NO.getDisplayValue());
            testPage.enter("haveVehicle", NO.getDisplayValue());
            testPage.enter("ownRealEstate", "No, I do not own any real estate");
            testPage.clickContinue();
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

            assertThat(getPdfFieldText(pdAcroForms.get(CCAP), "ADULT_REQUESTING_CHILDCARE_LOOKING_FOR_JOB_FULL_NAME_0")).isEqualTo("Pam Beesly");
            assertThat(getPdfFieldText(pdAcroForms.get(CCAP), "ADULT_REQUESTING_CHILDCARE_GOING_TO_SCHOOL_FULL_NAME_0")).isEqualTo("Dwight Schrute");
            assertThat(getPdfFieldText(pdAcroForms.get(CCAP), "ADULT_REQUESTING_CHILDCARE_WORKING_FULL_NAME_0")).isEqualTo("Pam Beesly");
            assertThat(getPdfFieldText(pdAcroForms.get(CCAP), "ADULT_REQUESTING_CHILDCARE_WORKING_EMPLOYERS_NAME_0")).isEqualTo("Pam's Employer");
            assertThat(getPdfFieldText(pdAcroForms.get(CCAP), "ADULT_REQUESTING_CHILDCARE_LOOKING_FOR_JOB_FULL_NAME_1")).isEmpty();
            assertThat(getPdfFieldText(pdAcroForms.get(CCAP), "ADULT_REQUESTING_CHILDCARE_GOING_TO_SCHOOL_FULL_NAME_1")).isEmpty();
            assertThat(getPdfFieldText(pdAcroForms.get(CCAP), "ADULT_REQUESTING_CHILDCARE_WORKING_FULL_NAME_1")).isEmpty();
            assertThat(getPdfFieldText(pdAcroForms.get(CCAP), "ADULT_REQUESTING_CHILDCARE_WORKING_EMPLOYERS_NAME_1")).isEmpty();
        }

        @Test
        void shouldMapLivingSituationToSelectedResponseForApplicant() {
            fillInRequiredPages();

            navigateTo("livingSituation");
            testPage.enter("livingSituation", "Staying in a hotel or motel");
            testPage.clickContinue();

            Map<Document, PDAcroForm> pdAcroForms = submitAndDownloadReceipt();

            assertThat(getPdfFieldText(pdAcroForms.get(CCAP), "LIVING_SITUATION")).isEqualTo("HOTEL_OR_MOTEL");
        }
    }

    @Nested
    class CAF {
        @BeforeEach
        void setUp() {
            selectPrograms(List.of("Cash programs"));
        }

        @Test
        void shouldMapPregnantHouseholdMembers() {
            addHouseholdMembers();

            testPage.clickButton("Yes, that's everyone");
            navigateTo("pregnant");
            testPage.enter("isPregnant", YesNoAnswer.YES.getDisplayValue());
            testPage.enter("whoIsPregnant", "Me");
            testPage.enter("whoIsPregnant", "Jim Halpert");
            testPage.clickContinue();

            PDAcroForm pdAcroForm = submitAndDownloadCaf();
            assertThat(getPdfFieldText(pdAcroForm, "WHO_IS_PREGNANT")).isEqualTo(
                    "Dwight Schrute, Jim Halpert"
            );
        }

        @Test
        void shouldMapPrograms() {
            navigateTo("choosePrograms");
            testPage.enter("programs", "Food (SNAP)");
            testPage.enter("programs", "Housing Support");
            testPage.enter("programs", "Emergency Assistance");
            testPage.clickContinue();

            PDAcroForm pdAcroForm = submitAndDownloadCaf();
            assertThat(getPdfFieldText(pdAcroForm, "FOOD")).isEqualTo("Yes");
            assertThat(getPdfFieldText(pdAcroForm, "CASH")).isEqualTo("Yes");
            assertThat(getPdfFieldText(pdAcroForm, "EMERGENCY")).isEqualTo("Yes");
            assertThat(getPdfFieldText(pdAcroForm, "GRH")).isEqualTo("Yes");
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
            assertThat(getPdfFieldText(pdAcroForm, "SNAP_EXPEDITED_ELIGIBILITY")).isEqualTo("Undetermined");
        }

        @Test
        void shouldAddAuthorizedRepFieldsIfYes() {
            navigateTo("authorizedRep");
            testPage.enter("communicateOnYourBehalf", YES.getDisplayValue());
            navigateTo("speakToCounty");
            testPage.enter("getMailNotices", YES.getDisplayValue());
            testPage.enter("spendOnYourBehalf", YES.getDisplayValue());

            testPage.enter("helpersFullName", "defaultFirstName defaultLastName");
            testPage.enter("helpersStreetAddress", "someStreetAddress");
            testPage.enter("helpersCity", "someCity");
            testPage.enter("helpersZipCode", "12345");
            testPage.enter("helpersPhoneNumber", "7234567890");
            testPage.clickContinue();

            PDAcroForm pdAcroForm = submitAndDownloadCaf();
            assertThat(getPdfFieldText(pdAcroForm, "AUTHORIZED_REP_FILL_OUT_FORM")).isEqualTo("Yes");
            assertThat(getPdfFieldText(pdAcroForm, "AUTHORIZED_REP_GET_NOTICES")).isEqualTo("Yes");
            assertThat(getPdfFieldText(pdAcroForm, "AUTHORIZED_REP_SPEND_ON_YOUR_BEHALF")).isEqualTo("Yes");
            assertThat(getPdfFieldText(pdAcroForm, "AUTHORIZED_REP_NAME")).isEqualTo("defaultFirstName defaultLastName");
            assertThat(getPdfFieldText(pdAcroForm, "AUTHORIZED_REP_ADDRESS")).isEqualTo("someStreetAddress");
            assertThat(getPdfFieldText(pdAcroForm, "AUTHORIZED_REP_CITY")).isEqualTo("someCity");
            assertThat(getPdfFieldText(pdAcroForm, "AUTHORIZED_REP_ZIP_CODE")).isEqualTo("12345");
            assertThat(getPdfFieldText(pdAcroForm, "AUTHORIZED_REP_PHONE_NUMBER")).isEqualTo("(723) 456-7890");


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
    }

    @Nested
    class CAFandCCAP {
        @BeforeEach
        void setUp() {
            selectPrograms(List.of("Child Care Assistance", "Cash programs"));
        }

        @Test
        void shouldMapPrograms() {
            PDAcroForm pdAcroForm = submitAndDownloadCaf();
            assertThat(getPdfFieldText(pdAcroForm, "CCAP")).isEqualTo("Yes");
            assertThat(getPdfFieldText(pdAcroForm, "CASH")).isEqualTo("Yes");
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
            List.of(CAF, CCAP).forEach(type -> {
                PDAcroForm pdAcroForm = pdAcroForms.get(type);
                assertThat(pdAcroForm.getField("APPLICANT_HOME_STREET_ADDRESS").getValueAsString())
                        .isEqualTo(originalStreetAddress);
                assertThat(pdAcroForm.getField("APPLICANT_HOME_CITY").getValueAsString())
                        .isEqualTo(originalCity);
                assertThat(pdAcroForm.getField("APPLICANT_HOME_STATE").getValueAsString())
                        .isEqualTo("MN");
                assertThat(pdAcroForm.getField("APPLICANT_HOME_ZIPCODE").getValueAsString())
                        .isEqualTo(originalZipCode);
            });

            assertThat(pdAcroForms.get(CAF).getField("APPLICANT_HOME_APT_NUMBER").getValueAsString())
                    .isEqualTo(originalApt);
        }

        @Test
        void shouldMapNoForSelfEmployment() {
            navigateTo("doYouLiveAlone");
            testPage.enter("liveAlone", YesNoAnswer.YES.getDisplayValue());
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

            assertThat(pdAcroForms.get(CCAP).getField("NON_SELF_EMPLOYMENT_EMPLOYERS_NAME_0").getValueAsString()).isEqualTo("someEmployerName");
            assertThat(pdAcroForms.get(CCAP).getField("NON_SELF_EMPLOYMENT_PAY_FREQUENCY_0").getValueAsString()).isEqualTo("Every week");
            assertThat(pdAcroForms.get(CCAP).getField("NON_SELF_EMPLOYMENT_GROSS_MONTHLY_INCOME_0").getValueAsString()).isEqualTo("4.0");
        }

        @Test
        void shouldMapUnearnedIncome() {
            navigateTo("unearnedIncome");
            testPage.enter("unearnedIncome", "Social Security");
            testPage.enter("unearnedIncome", "Child or Spousal support");
            testPage.clickContinue();

            testPage.enter("socialSecurityAmount", "10");
            testPage.enter("childOrSpousalSupportAmount", "20");
            testPage.clickContinue();

            Map<Document, PDAcroForm> pdAcroForms = submitAndDownloadReceipt();

            List.of(CAF, CCAP).forEach(type -> {
                        PDAcroForm pdAcroForm = pdAcroForms.get(type);
                        assertThat(pdAcroForm.getField("SOCIAL_SECURITY").getValueAsString()).isEqualTo("Yes");
                        assertThat(pdAcroForm.getField("CHILD_OR_SPOUSAL_SUPPORT").getValueAsString()).isEqualTo("Yes");
                        assertThat(pdAcroForm.getField("SSI").getValueAsString()).isEqualTo("No");
                        assertThat(pdAcroForm.getField("VETERANS_BENEFITS").getValueAsString()).isEqualTo("No");
                        assertThat(pdAcroForm.getField("UNEMPLOYMENT").getValueAsString()).isEqualTo("No");
                        assertThat(pdAcroForm.getField("WORKERS_COMPENSATION").getValueAsString()).isEqualTo("No");
                        assertThat(pdAcroForm.getField("RETIREMENT").getValueAsString()).isEqualTo("No");
                        assertThat(pdAcroForm.getField("TRIBAL_PAYMENTS").getValueAsString()).isEqualTo("No");
                    }
            );
            PDAcroForm ccap = pdAcroForms.get(CCAP);
            assertThat(ccap.getField("SOCIAL_SECURITY_AMOUNT").getValueAsString()).isEqualTo("10");
            assertThat(ccap.getField("CHILD_OR_SPOUSAL_SUPPORT_AMOUNT").getValueAsString()).isEqualTo("20");
            assertThat(ccap.getField("SUPPLEMENTAL_SECURITY_INCOME_AMOUNT").getValueAsString()).isEqualTo("");
            assertThat(ccap.getField("VETERANS_BENEFITS_AMOUNT").getValueAsString()).isEqualTo("");
            assertThat(ccap.getField("UNEMPLOYMENT_INSURANCE_AMOUNT").getValueAsString()).isEqualTo("");
            assertThat(ccap.getField("WORKERS_COMPENSATION_AMOUNT").getValueAsString()).isEqualTo("");
            assertThat(ccap.getField("RETIREMENT_BENEFITS_AMOUNT").getValueAsString()).isEqualTo("");
            assertThat(ccap.getField("TRIBAL_PAYMENTS_AMOUNT").getValueAsString()).isEqualTo("");

            assertThat(ccap.getField("SOCIAL_SECURITY_FREQUENCY").getValueAsString()).isEqualTo("Monthly");
            assertThat(ccap.getField("CHILD_OR_SPOUSAL_SUPPORT_FREQUENCY").getValueAsString()).isEqualTo("Monthly");
            assertThat(ccap.getField("SUPPLEMENTAL_SECURITY_INCOME_FREQUENCY").getValueAsString()).isEqualTo("");
            assertThat(ccap.getField("VETERANS_BENEFITS_FREQUENCY").getValueAsString()).isEqualTo("");
            assertThat(ccap.getField("UNEMPLOYMENT_INSURANCE_FREQUENCY").getValueAsString()).isEqualTo("");
            assertThat(ccap.getField("WORKERS_COMPENSATION_FREQUENCY").getValueAsString()).isEqualTo("");
            assertThat(ccap.getField("RETIREMENT_BENEFITS_FREQUENCY").getValueAsString()).isEqualTo("");
            assertThat(ccap.getField("TRIBAL_PAYMENTS_FREQUENCY").getValueAsString()).isEqualTo("");
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
                List.of(CAF, CCAP).forEach(type -> {
                    PDAcroForm pdAcroForm = pdAcroForms.get(type);
                    assertThat(pdAcroForm.getField("APPLICANT_MAILING_STREET_ADDRESS").getValueAsString())
                            .isEqualTo(originalStreetAddress);
                    assertThat(pdAcroForm.getField("APPLICANT_MAILING_CITY").getValueAsString())
                            .isEqualTo(originalCity);
                    assertThat(pdAcroForm.getField("APPLICANT_MAILING_STATE").getValueAsString())
                            .isEqualTo("MN");
                    assertThat(pdAcroForm.getField("APPLICANT_MAILING_ZIPCODE").getValueAsString())
                            .isEqualTo(originalZipCode);
                });
                assertThat(pdAcroForms.get(CAF).getField("APPLICANT_MAILING_APT_NUMBER").getValueAsString())
                        .isEqualTo(originalApt);
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

            @Test
            void shouldMapToEnrichedMailingAddressIfSameMailingAddressIsFalseAndUseEnrichedAddressIsTrue() {
                navigateTo("homeAddress");
                fillInAddress();
                testPage.enter("sameMailingAddress", "No, use a different address for mail");
                testPage.clickContinue();
                testPage.clickButton("Use this address");
                testPage.enter("streetAddress", "originalStreetAddress");
                testPage.enter("apartmentNumber", "originalApt");
                testPage.enter("city", "originalCity");
                testPage.enter("state", "IL");
                testPage.enter("zipCode", "54321");
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
        void shouldMapAdditionalIncomeInfo() {
            navigateTo("futureIncome");
            testPage.enter("earnLessMoneyThisMonth", YesNoAnswer.YES.getDisplayValue());
            driver.findElement(By.id("additionalIncomeInfo")).sendKeys("abc");
            testPage.clickContinue();

            Map<Document, PDAcroForm> pdAcroForms = submitAndDownloadReceipt();
            List.of(CAF, CCAP).forEach(type -> {
                PDAcroForm pdAcroForm = pdAcroForms.get(type);
                assertThat(getPdfFieldText(pdAcroForm, "ADDITIONAL_INCOME_INFO")).isEqualTo("abc");
            });
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
            assertThat(getPdfFieldText(pdAcroForms.get(CAF), "GROSS_MONTHLY_INCOME_0")).isEqualTo("123.0");
            assertThat(getPdfFieldText(pdAcroForms.get(CAF), "MONEY_MADE_LAST_MONTH")).isEqualTo("123.0");
            assertThat(getPdfFieldText(pdAcroForms.get(CAF), "SNAP_EXPEDITED_ELIGIBILITY")).isEqualTo("Expedited-SNAP");

            assertThat(getPdfFieldText(pdAcroForms.get(CCAP), "NON_SELF_EMPLOYMENT_GROSS_MONTHLY_INCOME_0")).isEqualTo("123.0");
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
            assertThat(getPdfFieldText(pdAcroForms.get(CAF), "GROSS_MONTHLY_INCOME_0")).isEqualTo("123.0");
            assertThat(getPdfFieldText(pdAcroForms.get(CAF), "MONEY_MADE_LAST_MONTH")).isEqualTo("123.0");
            assertThat(getPdfFieldText(pdAcroForms.get(CAF), "SNAP_EXPEDITED_ELIGIBILITY")).isEqualTo("Expedited-SNAP");

            assertThat(getPdfFieldText(pdAcroForms.get(CCAP), "NON_SELF_EMPLOYMENT_GROSS_MONTHLY_INCOME_0")).isEqualTo("123.0");
        }

        @Test
        void shouldMapAdditionalApplicationInformationTextToCAFAndCCAP() {
            navigateTo("additionalInfo");
            driver.findElement(By.id("additionalInfo")).sendKeys("Some additional information about my application");
            testPage.clickContinue();
            Map<Document, PDAcroForm> pdAcroForms = submitAndDownloadReceipt();
            assertThat(getPdfFieldText(pdAcroForms.get(CAF), "ADDITIONAL_APPLICATION_INFO")).isEqualTo("Some additional information about my application");
            assertThat(getPdfFieldText(pdAcroForms.get(CCAP), "ADDITIONAL_APPLICATION_INFO")).isEqualTo("Some additional information about my application");
        }

        @Test
        void shouldMapLivingSituationToUnknownIfNoneOfTheseIsSelected() {
            fillInRequiredPages();

            navigateTo("livingSituation");
            testPage.enter("livingSituation", "None of these");
            testPage.clickContinue();

            Map<Document, PDAcroForm> pdAcroForms = submitAndDownloadReceipt();

            assertThat(getPdfFieldText(pdAcroForms.get(CAF), "LIVING_SITUATION")).isEqualTo("UNKNOWN");
            assertThat(getPdfFieldText(pdAcroForms.get(CCAP), "LIVING_SITUATION")).isEqualTo("UNKNOWN");
        }

        @Test
        void shouldNotMapLivingSituationIfNotAnswered() {
            fillInRequiredPages();
            navigateTo("livingSituation");
            testPage.clickContinue();

            Map<Document, PDAcroForm> pdAcroForms = submitAndDownloadReceipt();

            assertThat(getPdfFieldText(pdAcroForms.get(CAF), "LIVING_SITUATION")).isEqualTo("Off");
            assertThat(getPdfFieldText(pdAcroForms.get(CCAP), "LIVING_SITUATION")).isEqualTo("Off");
        }

        @Test
        void shouldMapCcapExpeditedEligibility() {
            fillInRequiredPages();

            navigateTo("livingSituation");
            testPage.enter("livingSituation", "Staying in a hotel or motel");
            testPage.clickContinue();

            Map<Document, PDAcroForm> pdAcroForms = submitAndDownloadReceipt();

            assertThat(getPdfFieldText(pdAcroForms.get(CAF), "CCAP_EXPEDITED_ELIGIBILITY")).isEqualTo("Expedited-CCAP");
            assertThat(getPdfFieldText(pdAcroForms.get(CCAP), "CCAP_EXPEDITED_ELIGIBILITY")).isEqualTo("Expedited-CCAP");
        }
    }

    private void addHouseholdMembers() {
        navigateTo("personalInfo");
        testPage.enter("firstName", "Dwight");
        testPage.enter("lastName", "Schrute");
        testPage.clickContinue();

        navigateTo("doYouLiveAlone");
        testPage.enter("liveAlone", YesNoAnswer.NO.getDisplayValue());
        testPage.clickContinue();

        testPage.enter("firstName", "Jim");
        testPage.enter("lastName", "Halpert");
        testPage.enter("programs", "Child Care Assistance");
        testPage.clickContinue();

        testPage.clickButton("Add a person");

        testPage.enter("firstName", "Pam");
        testPage.enter("lastName", "Beesly");
        testPage.enter("programs", "Child Care Assistance");
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
            testPage.clickButton("Skip this for now");
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
        programs.forEach(program -> testPage.enter("programs", program));
        testPage.clickContinue();
    }
}
