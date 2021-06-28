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
    class CAFandCCAP {
        @BeforeEach
        void setUp() {
            selectPrograms(List.of(PROGRAM_SNAP, PROGRAM_CCAP, PROGRAM_CASH));
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
