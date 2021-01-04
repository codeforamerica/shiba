package org.codeforamerica.shiba.output.pdf;

import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.codeforamerica.shiba.AbstractBasePageTest;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.pages.SuccessPage;
import org.codeforamerica.shiba.pages.YesNoAnswer;
import org.codeforamerica.shiba.pages.enrichment.Address;
import org.codeforamerica.shiba.pages.enrichment.LocationClient;
import org.junit.jupiter.api.*;
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
    }

    @Test
    void shouldMapNoForUnearnedIncomeOptionsThatAreNotChecked() {
        navigateTo("unearnedIncome");
        testPage.enter("unearnedIncome", "Social Security");
        testPage.enter("unearnedIncome", "Child or Spousal support");
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
    }

    @Nested
    class EnergyAssistanceLIHEAP {
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

        Map<Document, PDAcroForm> pdAcroForms = submitAndDownloadReceipt();
        assertThat(pdAcroForms.get(CCAP).getField("STUDENT_FULL_NAME_0").getValueAsString())
                .isEqualTo("Dwight Schrute");
        assertThat(pdAcroForms.get(CCAP).getField("STUDENT_FULL_NAME_1").getValueAsString())
                .isEqualTo("Jim Halpert");
    }

    @Test
    void shouldMapPrograms() {
        navigateTo("choosePrograms");
        testPage.enter("programs", "Food (SNAP)");
        testPage.enter("programs", "Cash programs");
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
    void shouldMapNoPermanentAddressIfSelected() {
        navigateTo("homeAddress");
        fillInAddress();
        testPage.enter("isHomeless", "I don't have a permanent address");
        testPage.enter("sameMailingAddress", "Yes, send mail here");
        testPage.clickContinue();
        testPage.clickButton("Use this address");

        PDAcroForm pdAcroForm = submitAndDownloadCaf();
        assertThat(pdAcroForm.getField("APPLICANT_HOMELESS").getValueAsString())
                .isEqualTo("homeless");
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

        testPage.clickInputById("subtle-link");
        testPage.enter("lastThirtyDaysJobIncome", "123");
        testPage.clickContinue();

        Map<Document, PDAcroForm> pdAcroForms = submitAndDownloadReceipt();
        assertThat(getPdfFieldText(pdAcroForms.get(CAF), "GROSS_MONTHLY_INCOME_0")).isEqualTo("123.0");
        assertThat(getPdfFieldText(pdAcroForms.get(CAF), "MONEY_MADE_LAST_MONTH")).isEqualTo("123.0");
        assertThat(getPdfFieldText(pdAcroForms.get(CAF), "EXPEDITED_ELIGIBILITY")).isEqualTo("Expedited");

        assertThat(getPdfFieldText(pdAcroForms.get(CCAP), "NON_SELF_EMPLOYMENT_GROSS_MONTHLY_INCOME_0")).isEqualTo("123.0");
    }

    @Test
    void shouldMapJobLastThirtyDayIncomeAllBlankIsUndetermind() {
        addHouseholdMembers();

        fillInRequiredPages();

        navigateTo("incomeByJob");
        testPage.clickButton("Add a job");
        testPage.enter("whoseJobIsIt", "Jim Halpert");
        testPage.clickContinue();
        testPage.enter("employersName", "someEmployerName");
        testPage.clickContinue();
        testPage.enter("selfEmployment", "No");

        testPage.clickInputById("subtle-link");
        testPage.enter("lastThirtyDaysJobIncome", "");
        testPage.clickContinue();

        testPage.clickButton("Add a job");
        testPage.enter("whoseJobIsIt", "Me");
        testPage.clickContinue();
        testPage.enter("employersName", "someEmployerName");
        testPage.clickContinue();
        testPage.enter("selfEmployment", "No");

        testPage.clickInputById("subtle-link");
        testPage.enter("lastThirtyDaysJobIncome", "");
        testPage.clickContinue();

        PDAcroForm pdAcroForm = submitAndDownloadCaf();
        assertThat(getPdfFieldText(pdAcroForm, "EXPEDITED_ELIGIBILITY")).isEqualTo("Undetermined");
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

        testPage.clickInputById("subtle-link");
        testPage.enter("lastThirtyDaysJobIncome", "123");
        testPage.clickContinue();

        // Job 2
        testPage.clickButton("Add a job");
        testPage.enter("whoseJobIsIt", "Me");
        testPage.clickContinue();
        testPage.enter("employersName", "someEmployerName");
        testPage.clickContinue();
        testPage.enter("selfEmployment", "No");

        testPage.clickInputById("subtle-link");
        testPage.enter("lastThirtyDaysJobIncome", "");
        testPage.clickContinue();

        Map<Document, PDAcroForm> pdAcroForms = submitAndDownloadReceipt();
        assertThat(getPdfFieldText(pdAcroForms.get(CAF), "GROSS_MONTHLY_INCOME_0")).isEqualTo("123.0");
        assertThat(getPdfFieldText(pdAcroForms.get(CAF), "MONEY_MADE_LAST_MONTH")).isEqualTo("123.0");
        assertThat(getPdfFieldText(pdAcroForms.get(CAF), "EXPEDITED_ELIGIBILITY")).isEqualTo("Expedited");

        assertThat(getPdfFieldText(pdAcroForms.get(CCAP), "NON_SELF_EMPLOYMENT_GROSS_MONTHLY_INCOME_0")).isEqualTo("123.0");
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

    @Test
    void shouldMapAdultsRequestingChildcareAssistance() {
        addHouseholdMembers();

        navigateTo("jobSearch");
        testPage.enter("currentlyLookingForJob", "Yes");

        testPage.enter("whoIsLookingForAJob", "Jim Halpert");
        testPage.enter("whoIsLookingForAJob", "Pam Beesly");
        testPage.clickContinue();

        navigateTo("childrenInNeedOfCare");
        testPage.enter("whoNeedsChildCare", "Jim Halpert");
        testPage.clickContinue();

        Map<Document, PDAcroForm> pdAcroForms = submitAndDownloadReceipt();

        assertThat(getPdfFieldText(pdAcroForms.get(CCAP), "ADULT_REQUESTING_CHILDCARE_LOOKING_FOR_JOB_FULL_NAME_0")).isEqualTo("Jim Halpert");
        assertThat(getPdfFieldText(pdAcroForms.get(CCAP), "ADULT_REQUESTING_CHILDCARE_LOOKING_FOR_JOB_FULL_NAME_1")).isEqualTo("Pam Beesly");
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
        SuccessPage successPage = new SuccessPage(driver);
        successPage.downloadReceipt();
        await().until(() -> getAllFiles().size() == 2);

        return getAllFiles();
    }


    private PDAcroForm submitAndDownloadCaf() {
        return submitAndDownloadReceipt().get(CAF);
    }
}
