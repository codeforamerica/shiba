package org.codeforamerica.shiba.pages;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.codeforamerica.shiba.AbstractBasePageTest;
import org.codeforamerica.shiba.pages.emails.MailGunEmailClient;
import org.codeforamerica.shiba.pages.enrichment.Address;
import org.codeforamerica.shiba.pages.enrichment.smartystreets.SmartyStreetClient;
import org.codeforamerica.shiba.pages.events.PageEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openqa.selenium.By;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.jdbc.Sql;

import java.io.File;
import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.codeforamerica.shiba.output.Document.CAF;
import static org.codeforamerica.shiba.output.Document.CCAP;
import static org.codeforamerica.shiba.pages.YesNoAnswer.NO;
import static org.codeforamerica.shiba.pages.YesNoAnswer.YES;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Tag("integration")
public class UserJourneyPageTest extends AbstractBasePageTest {

    @MockBean
    Clock clock;

    @MockBean
    PageEventPublisher pageEventPublisher;

    @MockBean
    SmartyStreetClient smartyStreetClient;

    @MockBean
    MailGunEmailClient mailGunEmailClient;

    @Override
    @BeforeEach
    protected void setUp() throws IOException {
        super.setUp();
        driver.navigate().to(baseUrl);
        when(clock.instant()).thenReturn(Instant.now());
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
        when(smartyStreetClient.validateAddress(any())).thenReturn(Optional.empty());
    }

    @Test
    void intercomButtonIsPresent() {
        assertThat(driver.findElementById("intercom-frame")).isNotNull();
    }

    @Test
    void userCanCompleteTheNonExpeditedFlow() {
        nonExpeditedFlowToSuccessPage(false, true);
    }

    @Test
    void userCanCompleteTheNonExpeditedHouseholdFlow() {
        nonExpeditedFlowToSuccessPage(true, true);
    }

    @Test
    void userCanCompleteTheNonExpeditedFlowWithNoEmployment() {
        nonExpeditedFlowToSuccessPage(false, false);
    }

    @Test
    void userCanCompleteTheNonExpeditedHouseholdFlowWithNoEmployment() {
        nonExpeditedFlowToSuccessPage(true, false);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "123, 1233, A caseworker will contact you within 5-7 days to review your application.",
            "1, 1, A caseworker will contact you within 3 days to review your application."
    })
    void userCanCompleteTheExpeditedFlow(String moneyMadeLast30Days, String liquidAssets, String expeditedServiceDetermination) {
        completeFlowFromLandingPageThroughReviewInfo(List.of("Child Care Assistance"));
        testPage.clickLink("Submit application now with only the above information.");
        testPage.clickLink("Yes, I want to see if I qualify");

        testPage.enter("liveAlone", YES.getDisplayValue());
        testPage.enter("moneyMadeLast30Days", moneyMadeLast30Days);

        testPage.clickContinue();
        testPage.enter("haveSavings", YES.getDisplayValue());

        testPage.enter("liquidAssets", liquidAssets);

        testPage.clickContinue();
        testPage.enter("payRentOrMortgage", YES.getDisplayValue());

        testPage.enter("homeExpensesAmount", "333");
        testPage.clickContinue();

        testPage.enter("payForUtilities", "Cooling");
        testPage.clickContinue();

        testPage.enter("migrantOrSeasonalFarmWorker", NO.getDisplayValue());

        assertThat(driver.findElement(By.tagName("p")).getText()).contains(expeditedServiceDetermination);

        testPage.clickButton("Finish application");
        assertThat(testPage.getTitle()).isEqualTo("Legal Stuff");
        assertThat(driver.findElement(By.id("ccap-legal"))).isNotNull();
    }

    @ParameterizedTest
    @CsvSource(value = {
            "123, 1233, A caseworker will contact you within 5-7 days to review your application.",
            "1, 1, A caseworker will contact you within 3 days to review your application."
    })
    void userCanCompleteTheExpeditedFlowWithHousehold(String moneyMadeLast30Days, String liquidAssets, String expeditedServiceDetermination) {
        completeFlowFromLandingPageThroughReviewInfo(List.of("Child Care Assistance"));
        testPage.clickLink("Submit application now with only the above information.");
        testPage.clickLink("Yes, I want to see if I qualify");

        testPage.enter("liveAlone", NO.getDisplayValue());
        testPage.enter("moneyMadeLast30Days", moneyMadeLast30Days);

        testPage.clickContinue();
        testPage.enter("haveSavings", YES.getDisplayValue());

        testPage.enter("liquidAssets", liquidAssets);

        testPage.clickContinue();
        testPage.enter("payRentOrMortgage", YES.getDisplayValue());

        testPage.enter("homeExpensesAmount", "333");
        testPage.clickContinue();

        testPage.enter("payForUtilities", "Cooling");
        testPage.clickContinue();

        testPage.enter("migrantOrSeasonalFarmWorker", NO.getDisplayValue());

        assertThat(driver.findElement(By.tagName("p")).getText()).contains(expeditedServiceDetermination);

        testPage.clickButton("Finish application");
        assertThat(testPage.getTitle()).isEqualTo("Legal Stuff");
        assertThat(driver.findElement(By.id("ccap-legal"))).isNotNull();
    }

    @Test
    void shouldDownloadPDFWhenClickDownloadMyReceipt() {
        when(clock.instant()).thenReturn(Instant.ofEpochSecond(1243235L));
        SuccessPage successPage = nonExpeditedFlowToSuccessPage(false, true);
        successPage.downloadPdfs();

        await().until(() -> {
            File[] listFiles = path.toFile().listFiles();
            List<String> documentNames = Arrays.stream(listFiles).map(File::getName).collect(Collectors.toList());

            return List.of(CAF, CCAP).stream().map(document -> documentNames.stream().anyMatch(documentName ->
                    documentName.contains("_MNB_") && documentName.endsWith(".pdf") &&
                    documentName.contains(document.toString())
            )).collect(Collectors.toList()).stream().allMatch(assertion -> assertion.equals(true));
        });
    }

    @Test
    @Sql(statements = "TRUNCATE TABLE applications;")
    void shouldCaptureMetricsAfterAnApplicationIsCompleted() {
        when(clock.instant()).thenReturn(
                LocalDateTime.of(2020, 1, 1, 10, 10).atOffset(ZoneOffset.UTC).toInstant(),
                LocalDateTime.of(2020, 1, 1, 10, 15, 30).atOffset(ZoneOffset.UTC).toInstant()
        );
        SuccessPage successPage = nonExpeditedFlowToSuccessPage(false, true);
        successPage.chooseSentiment(Sentiment.HAPPY);
        successPage.submitFeedback();

        driver.navigate().to(baseUrl + "/metrics");
        MetricsPage metricsPage = new MetricsPage(driver);
        assertThat(metricsPage.getCardValue("Applications Submitted")).isEqualTo("1");
        assertThat(metricsPage.getCardValue("Median All Time")).contains("05m 30s");
        assertThat(metricsPage.getCardValue("Median Week to Date")).contains("05m 30s");
        assertThat(metricsPage.getCardValue("Average Week to Date")).contains("05m 30s");
        assertThat(driver.findElements(By.tagName("td")).get(0).getText()).isEqualTo("Hennepin");
        assertThat(driver.findElements(By.tagName("td")).get(1).getText()).isEqualTo("0");
        assertThat(driver.findElements(By.tagName("td")).get(2).getText()).isEqualTo("0");
        assertThat(metricsPage.getCardValue("Happy")).contains("100%");
    }

    @Test
    void partialFlow() throws IOException {
        testPage.clickButton("Apply now");
        testPage.clickContinue();
        testPage.enter("writtenLanguage", "English");
        testPage.enter("spokenLanguage", "English");
        testPage.enter("needInterpreter", "Yes");
        testPage.clickContinue();
        testPage.enter("programs", "Emergency Assistance");
        testPage.clickContinue();
        testPage.clickContinue();
        fillOutPersonalInfo();
        testPage.clickContinue();
        navigateTo("signThisApplication");
        testPage.enter("applicantSignature", "some name");
        testPage.clickButton("Submit");

        completeDocumentUploadFlow();

        SuccessPage successPage = new SuccessPage(driver);
        successPage.downloadPdfs();
        await().until(() -> {
            File[] listFiles = path.toFile().listFiles();
            return Arrays.stream(listFiles).anyMatch(file -> file.getName().contains("_MNB_") && file.getName().endsWith(".pdf"));
        });

        File pdfFile = Arrays.stream(path.toFile().listFiles()).findFirst().orElseThrow();
        PDAcroForm acroForm = PDDocument.load(pdfFile).getDocumentCatalog().getAcroForm();
        assertThat(acroForm.getField("APPLICANT_WRITTEN_LANGUAGE_PREFERENCE").getValueAsString())
                .isEqualTo("ENGLISH");
        assertThat(acroForm.getField("APPLICANT_WRITTEN_LANGUAGE_PREFERENCE").getValueAsString())
                .isEqualTo("ENGLISH");
        assertThat(acroForm.getField("NEED_INTERPRETER").getValueAsString())
                .isEqualTo("Yes");
    }

    @Test
    void shouldSkipChildcareAssistancePageIfCCAPNotSelected() {
        completeFlowFromLandingPageThroughReviewInfo(List.of("Food (SNAP)"));
        testPage.clickLink("This looks correct");
        testPage.enter("liveAlone", NO.getDisplayValue());
        testPage.clickContinue();
        fillOutHousemateInfo("Emergency Assistance");
        testPage.clickContinue();
        testPage.clickButton("Yes, that's everyone");
        testPage.enter("isPreparingMealsTogether", NO.getDisplayValue());
    }

    @Test
    void shouldSkipWhoIsGoingToSchoolPageIfCCAPNotSelected() {
        completeFlowFromLandingPageThroughReviewInfo(List.of("Food (SNAP)"));
        testPage.clickLink("This looks correct");
        testPage.enter("liveAlone", NO.getDisplayValue());
        testPage.clickContinue();
        fillOutHousemateInfo("Emergency Assistance");
        testPage.clickContinue();
        testPage.clickButton("Yes, that's everyone");
        assertThat(driver.findElementByClassName("h2").getText().equals("Does everyone in your household buy and prepare food with you?"));
        testPage.clickButton(NO.getDisplayValue());
        testPage.clickButton(YES.getDisplayValue());
        assertThat(driver.findElementByClassName("h2").getText().equals("Is anyone in your household pregnant?"));
    }

    @Test
    void shouldAskWhoIsGoingToSchoolAndWhoIsLookingForWorkWhenCCAPIsSelectedInPrograms() {
        completeFlowFromLandingPageThroughReviewInfo(List.of("Child Care Assistance"));
        testPage.clickLink("This looks correct");
        testPage.enter("liveAlone", NO.getDisplayValue());
        testPage.clickContinue();
        fillOutHousemateInfo("Emergency Assistance");
        testPage.clickContinue();
        testPage.clickButton("Yes, that's everyone");
        assertThat(driver.getTitle()).isEqualTo("Who are the children in need of care?");
        testPage.clickContinue();
        testPage.clickButton(YES.getDisplayValue());
        assertThat(driver.getTitle()).isEqualTo("Who is going to school?");
        testPage.clickContinue();
        testPage.clickButton(NO.getDisplayValue());
        testPage.clickButton(YES.getDisplayValue());
        testPage.clickButton(YES.getDisplayValue());
        testPage.clickButton(NO.getDisplayValue());
        testPage.clickButton(NO.getDisplayValue());
        testPage.clickContinue();
        testPage.clickButton(NO.getDisplayValue());
        testPage.clickButton(YES.getDisplayValue());
        assertThat(driver.getTitle()).isEqualTo("Who is looking for a job");
    }

    @Test
    void shouldAskWhoIsGoingToSchoolAndWhoIsLookingForWorkWhenCCAPIsSelectedInHouseholdMemberInfo() {
        completeFlowFromLandingPageThroughReviewInfo(List.of("Food (SNAP)"));
        testPage.clickLink("This looks correct");
        testPage.enter("liveAlone", NO.getDisplayValue());
        testPage.clickContinue();
        fillOutHousemateInfo("Child Care Assistance");
        testPage.clickContinue();
        testPage.clickButton("Yes, that's everyone");
        assertThat(driver.getTitle()).isEqualTo("Who are the children in need of care?");
        testPage.clickContinue();
        testPage.clickButton(YES.getDisplayValue());
        testPage.clickButton(YES.getDisplayValue());
        assertThat(driver.getTitle()).isEqualTo("Who is going to school?");
        testPage.clickContinue();
        testPage.clickButton(NO.getDisplayValue());
        testPage.clickButton(YES.getDisplayValue());
        testPage.clickButton(YES.getDisplayValue());
        testPage.clickButton(NO.getDisplayValue());
        testPage.clickButton(NO.getDisplayValue());
        testPage.clickContinue();
        testPage.clickButton(NO.getDisplayValue());
        testPage.clickButton(YES.getDisplayValue());
        assertThat(driver.getTitle()).isEqualTo("Who is looking for a job");
    }

    @Test
    void shouldSkipWhoIsGoingToSchoolAndWhoIsLookingForWorkPageIfCCAPSelectedButLiveAloneIsTrue() {
        completeFlowFromLandingPageThroughReviewInfo(List.of("Child Care Assistance"));
        testPage.clickLink("This looks correct");
        testPage.enter("liveAlone", YES.getDisplayValue());
        testPage.clickContinue();
        testPage.clickButton(YES.getDisplayValue());
        assertThat(driver.getTitle()).isEqualTo("Pregnant");
        testPage.clickButton(YES.getDisplayValue());
        testPage.clickButton(YES.getDisplayValue());
        testPage.clickButton(YES.getDisplayValue());
        testPage.clickButton(YES.getDisplayValue());
        testPage.clickButton(YES.getDisplayValue());
        testPage.clickContinue();
        testPage.clickButton(NO.getDisplayValue());
        testPage.clickButton(YES.getDisplayValue());
        assertThat(driver.getTitle()).isEqualTo("Income Up Next");
    }

    @Test
    void shouldSkipWhoIsLookingForWorkPageIfCCAPIsNotSelectedInHouseholdOrPrograms() {
        completeFlowFromLandingPageThroughReviewInfo(List.of("Food (SNAP)"));
        testPage.clickLink("This looks correct");
        testPage.enter("liveAlone", NO.getDisplayValue());
        testPage.clickContinue();
        fillOutHousemateInfo("Emergency Assistance");
        testPage.clickContinue();
        testPage.clickButton("Yes, that's everyone");
        testPage.clickButton(YES.getDisplayValue());
        testPage.clickButton(NO.getDisplayValue());
        testPage.clickButton(NO.getDisplayValue());
        testPage.clickButton(NO.getDisplayValue());
        testPage.clickButton(YES.getDisplayValue());
        testPage.clickButton(NO.getDisplayValue());
        testPage.clickButton(NO.getDisplayValue());
        testPage.clickContinue();
        testPage.clickButton(NO.getDisplayValue());
        assertThat(driver.getTitle()).isEqualTo("Income Up Next");
    }

    @Test
    void shouldValidateContactInfoEmailEvenIfEmailNotSelected() {
        completeFlowFromLandingPageThroughContactInfo(List.of("Child Care Assistance"));
        testPage.enter("phoneNumber", "7234567890");
        testPage.enter("email", "email.com");
        testPage.enter("phoneOrEmail", "Text me");
        testPage.clickContinue();

        assertThat(driver.getTitle()).isEqualTo("Contact Info");
    }

    @Test
    void shouldShowCCAPInLegalStuffWhenHousholdSelectsCCAP(){
        completeFlowFromLandingPageThroughReviewInfo(List.of("Emergency Assistance"));
        testPage.clickLink("This looks correct");
        testPage.enter("liveAlone", NO.getDisplayValue());
        testPage.clickContinue();
        fillOutHousemateInfo("Child Care Assistance");
        testPage.clickContinue();
        testPage.clickButton("Yes, that's everyone");

        navigateTo("legalStuff");
        assertThat(driver.findElement(By.id("ccap-legal"))).isNotNull();
    }

    @Test
    void shouldNotShowCCAPInLegalStuffWhenNotSelectedByAnyone(){
        completeFlowFromLandingPageThroughReviewInfo(List.of("Emergency Assistance"));
        testPage.clickLink("This looks correct");
        testPage.enter("liveAlone", NO.getDisplayValue());
        testPage.clickContinue();
        fillOutHousemateInfo("Emergency Assistance");
        testPage.clickContinue();
        testPage.clickButton("Yes, that's everyone");

        navigateTo("legalStuff");
        assertThat(driver.findElements(By.id("ccap-legal"))).isEmpty();
    }

    private void completeFlowFromLandingPageThroughContactInfo(List<String> programSelections) {
        testPage.clickButton("Apply now");
        testPage.clickContinue();
        testPage.enter("writtenLanguage", "English");
        testPage.enter("spokenLanguage", "English");
        testPage.enter("needInterpreter", "Yes");
        testPage.clickContinue();
        programSelections.forEach(program -> testPage.enter("programs", program));
        testPage.clickContinue();
        testPage.clickContinue();

        fillOutPersonalInfo();

        testPage.clickContinue();
    }

    private void completeFlowFromLandingPageThroughReviewInfo(List<String> programSelections) {
        completeFlowFromLandingPageThroughContactInfo(programSelections);

        testPage.enter("phoneNumber", "7234567890");
        testPage.enter("email", "some@email.com");
        testPage.enter("phoneOrEmail", "Text me");
        testPage.clickContinue();
        testPage.enter("zipCode", "12345");
        testPage.enter("city", "someCity");
        testPage.enter("streetAddress", "someStreetAddress");
        testPage.enter("apartmentNumber", "someApartmentNumber");
        testPage.enter("isHomeless", "I don't have a permanent address");
        testPage.enter("sameMailingAddress", "No, use a different address for mail");
        testPage.clickContinue();

        testPage.clickButton("Use this address");
        testPage.enter("zipCode", "12345");
        testPage.enter("city", "someCity");
        testPage.enter("streetAddress", "someStreetAddress");
        testPage.enter("state", "IL");
        testPage.enter("apartmentNumber", "someApartmentNumber");
        when(smartyStreetClient.validateAddress(any())).thenReturn(
                Optional.of(new Address("smarty street", "City", "CA", "03104", "", "someCounty"))
        );
        testPage.clickContinue();

        testPage.clickInputById("enriched-address");
        testPage.clickContinue();
        assertThat(driver.findElementById("mailing-address_street").getText()).isEqualTo("smarty street");
    }

    private void fillOutPersonInfo() {
        testPage.enter("firstName", "defaultFirstName");
        testPage.enter("lastName", "defaultLastName");
        testPage.enter("otherName", "defaultOtherName");
        testPage.enter("dateOfBirth", "01/12/1928");
        testPage.enter("ssn", "123456789");
        testPage.enter("maritalStatus", "Never married");
        testPage.enter("sex", "Female");
        testPage.enter("livedInMnWholeLife", "Yes");
        testPage.enter("moveToMnDate", "02/18/1776");
    }

    private void fillOutPersonalInfo() {
        fillOutPersonInfo();
        testPage.enter("moveToMnPreviousCity", "Chicago");
    }

    private SuccessPage nonExpeditedFlowToSuccessPage(boolean hasHousehold, boolean isWorking) {
        completeFlowFromLandingPageThroughReviewInfo(List.of("Child Care Assistance", "Cash programs"));
        testPage.clickLink("This looks correct");

        if (hasHousehold) {
            testPage.enter("liveAlone", NO.getDisplayValue());
            testPage.clickContinue();
            fillOutHousemateInfo("Child Care Assistance");
            testPage.clickContinue();
            testPage.clickButton("Yes, that's everyone");
            testPage.enter("whoNeedsChildCare", "defaultFirstName defaultLastName");
            testPage.clickContinue();
            testPage.enter("goingToSchool", NO.getDisplayValue());
            testPage.enter("isPregnant", YES.getDisplayValue());
            testPage.enter("whoIsPregnant", "Me");
            testPage.clickContinue();
        } else {
            testPage.enter("liveAlone", YES.getDisplayValue());
            testPage.clickContinue();
            testPage.enter("goingToSchool", NO.getDisplayValue());
            testPage.enter("isPregnant", NO.getDisplayValue());
        }

        testPage.enter("migrantOrSeasonalFarmWorker", NO.getDisplayValue());
        if (hasHousehold) {
            testPage.enter("isUsCitizen", NO.getDisplayValue());
            testPage.enter("whoIsNonCitizen", "Me");
            testPage.clickContinue();
        } else {
            testPage.enter("isUsCitizen", YES.getDisplayValue());
        }
        testPage.enter("hasDisability", NO.getDisplayValue());
        testPage.enter("hasWorkSituation", NO.getDisplayValue());
        testPage.clickContinue();

        if (isWorking) {
            testPage.enter("areYouWorking", YES.getDisplayValue());
            testPage.clickButton("Add a job");

            if (hasHousehold) {
                testPage.enter("whoseJobIsIt", "defaultFirstName defaultLastName");
                testPage.clickContinue();
            }

            testPage.enter("employersName", "some employer");
            testPage.clickContinue();
            testPage.enter("selfEmployment", YES.getDisplayValue());
            paidByTheHourOrSelectPayPeriod();
            testPage.enter("currentlyLookingForJob", NO.getDisplayValue());
        } else {
            testPage.enter("areYouWorking", NO.getDisplayValue());
            testPage.enter("currentlyLookingForJob", YES.getDisplayValue());

            if (hasHousehold) {
                testPage.enter("whoIsLookingForAJob", "defaultFirstName defaultLastName");
                testPage.clickContinue();
            }
        }

        testPage.clickContinue();
        testPage.enter("unearnedIncome", "Social Security");
        testPage.clickContinue();
        testPage.enter("socialSecurityAmount", "200");
        testPage.clickContinue();
        testPage.enter("earnLessMoneyThisMonth", "Yes");
        testPage.clickContinue();
        testPage.clickContinue();
        testPage.enter("homeExpenses", "Rent");
        testPage.clickContinue();
        testPage.enter("homeExpensesAmount", "123321");
        testPage.clickContinue();
        testPage.enter("payForUtilities", "Heating");
        testPage.clickContinue();
        testPage.enter("energyAssistance", YES.getDisplayValue());
        testPage.enter("energyAssistanceMoreThan20", YES.getDisplayValue());
        testPage.enter("supportAndCare", YES.getDisplayValue());
        testPage.enter("haveSavings", YES.getDisplayValue());
        testPage.enter("liquidAssets", "1234");
        testPage.clickContinue();
        testPage.enter("haveInvestments", NO.getDisplayValue());
        testPage.enter("haveVehicle", YES.getDisplayValue());
        testPage.enter("haveSoldAssets", NO.getDisplayValue());
        testPage.clickContinue();
        testPage.enter("registerToVote", "Yes, send me more info");
        completeHelperWorkflow();
        driver.findElement(By.id("additionalInfo")).sendKeys("Some additional information about my application");
        testPage.clickContinue();
        testPage.enter("agreeToTerms", "I agree");
        testPage.clickContinue();
        testPage.enter("applicantSignature", "some name");
        testPage.clickButton("Submit");

        completeDocumentUploadFlow();

        return new SuccessPage(driver);
    }

    private void completeDocumentUploadFlow() {
        testPage.clickButton("Upload documents now");
        testPage.clickButton("I'm finished uploading");
    }

    private void fillOutHousemateInfo(String programSelection) {
        testPage.enter("relationship", "housemate");
        testPage.enter("programs", programSelection);
        fillOutPersonInfo(); // need to fill out programs checkbox set above first
        testPage.enter("moveToMnPreviousState", "Illinois");
    }

    private void paidByTheHourOrSelectPayPeriod() {
        if (new Random().nextBoolean()) {
            testPage.enter("paidByTheHour", YES.getDisplayValue());
            testPage.enter("hourlyWage", "1");
            testPage.clickContinue();
            testPage.enter("hoursAWeek", "30");
        } else {
            testPage.enter("paidByTheHour", NO.getDisplayValue());
            testPage.enter("payPeriod", "Twice a month");
            testPage.clickContinue();
            testPage.enter("incomePerPayPeriod", "1");
        }
        testPage.clickContinue();
        testPage.goBack();
        testPage.clickButton("No, I'd rather keep going");
        testPage.clickButton("No, that's it.");
    }

    private void fillOutHelperInfo() {
        testPage.enter("helpersFullName", "defaultFirstName defaultLastName");
        testPage.enter("helpersStreetAddress", "someStreetAddress");
        testPage.enter("helpersCity", "someCity");
        testPage.enter("helpersZipCode", "12345");
        testPage.enter("helpersPhoneNumber", "7234567890");
        testPage.clickContinue();
    }

    private void completeHelperWorkflow() {
        if (new Random().nextBoolean()) {
            testPage.enter("helpWithBenefits", YES.getDisplayValue());
            testPage.enter("communicateOnYourBehalf", YES.getDisplayValue());
            testPage.enter("getMailNotices", YES.getDisplayValue());
            testPage.enter("spendOnYourBehalf", YES.getDisplayValue());
            fillOutHelperInfo();
        } else {
            testPage.enter("helpWithBenefits", NO.getDisplayValue());
        }
    }
}
