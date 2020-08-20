package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.output.ApplicationDataConsumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openqa.selenium.By;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.jdbc.Sql;

import java.io.IOException;
import java.time.*;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.codeforamerica.shiba.pages.DatePart.*;
import static org.codeforamerica.shiba.pages.YesNoAnswer.NO;
import static org.codeforamerica.shiba.pages.YesNoAnswer.YES;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class UserJourneyPageTest extends AbstractBasePageTest {

    @MockBean
    Clock clock;

    @MockBean
    ApplicationDataConsumer applicationDataConsumer;

    @MockBean
    ApplicationIdGenerator applicationIdGenerator;

    @Override
    @BeforeEach
    void setUp() throws IOException {
        super.setUp();
        driver.navigate().to(baseUrl + "/pages/landing");
        when(clock.instant()).thenReturn(Instant.now());
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
        when(applicationDataConsumer.process(any())).thenReturn(ZonedDateTime.now());
        when(applicationIdGenerator.generate()).thenReturn("123000FAKE");
    }

    @Test
    void userCanCompleteTheNonExpeditedFlow() {
        nonExpeditedFlowToSuccessPage();
    }

    @ParameterizedTest
    @CsvSource(value = {
            "123, 1233, A caseworker will contact you within 5-7 days to review your application.",
            "1, 1, A caseworker will contact you within 3 days to review your application."
    })
    void userCanCompleteTheExpeditedFlow(String moneyMadeLast30Days, String liquidAssets, String expeditedServiceDetermination) {
        completeFlowFromLandingPageToReviewInfo();
        driver.findElement(By.linkText("Submit application now with only the above information.")).click();
        driver.findElement(By.linkText("Yes, I want to see if I qualify")).click();

        Page expeditedIncomePage = testPage.choose(YES);
        expeditedIncomePage.enterInput("moneyMadeLast30Days", moneyMadeLast30Days);

        Page hasLiquidAssetPage = expeditedIncomePage.clickPrimaryButton();
        Page liquidAssetsPage = hasLiquidAssetPage.choose(YES);

        liquidAssetsPage.enterInput("liquidAssets", liquidAssets);

        Page expeditedExpensesPage = liquidAssetsPage.clickPrimaryButton();
        Page expeditedExpensesAmountPage = expeditedExpensesPage.choose(YES);

        expeditedExpensesAmountPage.enterInput("expeditedExpensesAmount", "333");
        Page expeditedUtilityPaymentsPage = expeditedExpensesAmountPage.clickPrimaryButton();

        expeditedUtilityPaymentsPage.selectEnumeratedInput("payForUtilities", "Cooling");
        Page expeditedMigrantFarmWorkerPage = expeditedUtilityPaymentsPage.clickPrimaryButton();

        Page expeditedDeterminationPage = expeditedMigrantFarmWorkerPage.choose(NO);

        assertThat(driver.findElement(By.tagName("p")).getText()).contains(expeditedServiceDetermination);

        Page importantToKnow = expeditedDeterminationPage.clickPrimaryButton();
        assertThat(importantToKnow.getTitle()).isEqualTo("Important to Know");
    }

    @Test
    void shouldCaptureSubmissionTimeAndApplicationIdOnSuccessPage() {
        when(applicationDataConsumer.process(any()))
                .thenReturn(ZonedDateTime.of(LocalDateTime.of(2020, 1, 1, 10, 10), ZoneOffset.UTC));

        SuccessPage successPage = nonExpeditedFlowToSuccessPage();

        assertThat(successPage.getTitle()).isEqualTo("Success");
        assertThat(successPage.getSubmissionTime()).contains("January 1, 2020");
        assertThat(successPage.getApplicationId()).contains("123000FAKE");
    }

    @Test
    void shouldDownloadPDFWhenClickDownloadMyReceipt() {
        when(clock.instant()).thenReturn(Instant.ofEpochSecond(1243235L));

        SuccessPage successPage = nonExpeditedFlowToSuccessPage();

        successPage.downloadReceipt();
        await().until(() -> path.resolve("1243235-DHS-5223.pdf").toFile().exists());
    }

    @Test
    void shouldDownloadXMLWhenClickDownloadXML() {
        when(clock.instant()).thenReturn(Instant.ofEpochSecond(1243235L));

        SuccessPage successPage = nonExpeditedFlowToSuccessPage();

        successPage.downloadXML();
        await().until(() -> path.resolve("1243235-ApplyMN.xml").toFile().exists());
    }

    @Test
    @Sql(statements = "TRUNCATE TABLE application_metrics;")
    void shouldCaptureMetricsAfterAnApplicationIsCompleted() {
        when(clock.instant()).thenReturn(
                LocalDateTime.of(2020, 1, 1, 10, 10).atOffset(ZoneOffset.UTC).toInstant(),
                LocalDateTime.of(2020, 1, 1, 10, 15, 30).atOffset(ZoneOffset.UTC).toInstant()
        );
        nonExpeditedFlowToSuccessPage();

        driver.navigate().to(baseUrl + "/metrics");
        MetricsPage metricsPage = new MetricsPage(driver);
        assertThat(metricsPage.getCardValue("Applications Submitted")).isEqualTo("1");
        assertThat(metricsPage.getCardValue("Completion Time")).contains("05m 30s");
    }

    private void completeFlowFromLandingPageToReviewInfo() {
        Page languagePreferencesPage = testPage
                .clickPrimaryButton()
                .clickPrimaryButton();
        languagePreferencesPage.selectFromDropdown("writtenLanguage", "English");
        languagePreferencesPage.selectFromDropdown("spokenLanguage", "English");
        languagePreferencesPage.selectEnumeratedInput("needInterpreter", "Yes");
        Page chooseProgramPage = languagePreferencesPage
                .clickPrimaryButton();
        chooseProgramPage.selectEnumeratedInput("programs", "Emergency assistance");
        Page howItWorksPage = chooseProgramPage.clickPrimaryButton();
        Page personalInfoPage = howItWorksPage.clickPrimaryButton().clickPrimaryButton();
        personalInfoPage.enterInput("firstName", "defaultFirstName");
        personalInfoPage.enterInput("lastName", "defaultLastName");
        personalInfoPage.enterInput("otherName", "defaultOtherName");
        personalInfoPage.enterDateInput("dateOfBirth", MONTH, "01");
        personalInfoPage.enterDateInput("dateOfBirth", DAY, "12");
        personalInfoPage.enterDateInput("dateOfBirth", YEAR, "1928");
        personalInfoPage.enterInput("ssn", "123456789");
        personalInfoPage.selectEnumeratedInput("maritalStatus", "Never married");
        personalInfoPage.selectEnumeratedInput("sex", "Female");
        personalInfoPage.selectEnumeratedInput("livedInMnWholeLife", "Yes");
        personalInfoPage.enterDateInput("moveToMnDate", MONTH, "02");
        personalInfoPage.enterDateInput("moveToMnDate", DAY, "18");
        personalInfoPage.enterDateInput("moveToMnDate", YEAR, "1776");
        personalInfoPage.enterInput("moveToMnPreviousCity", "Chicago");

        Page contactInfoPage = personalInfoPage.clickPrimaryButton();
        contactInfoPage.enterInput("phoneNumber", "7234567890");
        contactInfoPage.enterInput("email", "some@email.com");
        contactInfoPage.selectEnumeratedInput("phoneOrEmail", "Text me");

        Page homeAddressPage = contactInfoPage.clickPrimaryButton();

        homeAddressPage.enterInput("zipCode", "12345");
        homeAddressPage.enterInput("city", "someCity");
        homeAddressPage.enterInput("streetAddress", "someStreetAddress");
        homeAddressPage.enterInput("apartmentNumber", "someApartmentNumber");
        homeAddressPage.selectEnumeratedInput("isHomeless", "I’m homeless right now");
        homeAddressPage.selectEnumeratedInput("sameMailingAddress", "No, use a different address for mail");
        Page mailingAddressPage = homeAddressPage.clickPrimaryButton();

        mailingAddressPage.enterInput("zipCode", "12345");
        mailingAddressPage.enterInput("city", "someCity");
        mailingAddressPage.enterInput("streetAddress", "someStreetAddress");
        mailingAddressPage.enterInput("state", "IL");
        mailingAddressPage.enterInput("apartmentNumber", "someApartmentNumber");
        mailingAddressPage.clickPrimaryButton();
    }

    private SuccessPage nonExpeditedFlowToSuccessPage() {
        completeFlowFromLandingPageToReviewInfo();
        driver.findElement(By.linkText("This looks correct")).click();
        Page introPersonDetailsPage = testPage.choose(YES);
        Page goingToSchool = introPersonDetailsPage.clickPrimaryButton();
        Page pregnant = goingToSchool.choose(NO);
        Page migrantWorker = pregnant.choose(NO);
        Page usCitizen = migrantWorker.choose(NO);
        Page disability = usCitizen.choose(NO);
        Page workSituation = disability.choose(NO);
        Page introIncome = workSituation.choose(NO);
        Page employmentStatus = introIncome.clickPrimaryButton();
        Page incomeByJob = employmentStatus.choose(YES);
        Page employerName = incomeByJob.clickPrimaryButton();
        employerName.enterInput("employersName", "some employer");
        Page selfEmployment = employerName.clickPrimaryButton();
        Page paidByTheHourPage = selfEmployment.choose(YES);
        Page incomeUpNext = paidByTheHourOrSelectPayPeriod();
        Page unearnedIncome = incomeUpNext.clickPrimaryButton();
        unearnedIncome.selectEnumeratedInput("unearnedIncome", "Social Security");
        Page unearnedIncomeSources = unearnedIncome.clickPrimaryButton();
        unearnedIncomeSources.enterInput("socialSecurityAmount", "200");
        Page futureIncome = unearnedIncomeSources.clickPrimaryButton();
        futureIncome.selectEnumeratedInput("earnLessMoneyThisMonth", "Yes");
        Page startExpenses = futureIncome.clickPrimaryButton();
        Page homeExpenses = startExpenses.clickPrimaryButton();
        homeExpenses.selectEnumeratedInput("homeExpenses", "Rent");
        Page homeExpensesAmount = homeExpenses.clickPrimaryButton();
        homeExpensesAmount.enterInput("homeExpensesAmount", "123321");
        Page utilities = homeExpensesAmount.clickPrimaryButton();
        utilities.selectEnumeratedInput("payForUtilities", "Heating");
        Page energyAssistance = utilities.clickPrimaryButton();
        Page energyAssistanceMoreThan20 = energyAssistance.choose(YES);
        Page supportAndCare = energyAssistanceMoreThan20.choose(YES);
        Page savings = supportAndCare.choose(YES);
        Page savingsAmount = savings.choose(YES);
        savingsAmount.enterInput("liquidAssets", "1234");
        Page investments = savingsAmount.clickPrimaryButton();
        Page vehicle = investments.choose(NO);
        Page soldAssets = vehicle.choose(YES);
        Page submittingApplication = soldAssets.choose(NO);
        Page registerToVote = submittingApplication.clickPrimaryButton();
        Page importantToKnow = registerToVote.choose(YES);
        Page legalStuff = importantToKnow.clickPrimaryButton();
        legalStuff.selectEnumeratedInput("agreeToTerms", "I agree");
        Page signThisApplicationPage = legalStuff.clickPrimaryButton();

        signThisApplicationPage.enterInput("applicantSignature", "some name");
        signThisApplicationPage.clickPrimaryButton();

        return new SuccessPage(driver);
    }

    private Page paidByTheHourOrSelectPayPeriod() {
        Page paidByTheHourPage = testPage;
        if (new Random().nextBoolean()) {
            Page hourlyWage = paidByTheHourPage.choose(YES);
            hourlyWage.enterInput("hourlyWage", "1");
            Page hoursAWeek = hourlyWage.clickPrimaryButton();
            hoursAWeek.enterInput("hoursAWeek", "30");
            Page jobBuilder = hoursAWeek.clickPrimaryButton();
            return jobBuilder.clickPrimaryButton();
        } else {
            Page payPeriod = paidByTheHourPage.choose(NO);
            payPeriod.selectEnumeratedInput("payPeriod", "Twice a month");
            Page payPerPeriod = payPeriod.clickPrimaryButton();
            payPerPeriod.enterInput("incomePerPayPeriod", "1");
            Page jobBuilder = payPerPeriod.clickPrimaryButton();
            return jobBuilder.clickPrimaryButton();
        }
    }
}
