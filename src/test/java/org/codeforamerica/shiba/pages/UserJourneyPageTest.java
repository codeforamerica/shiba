package org.codeforamerica.shiba.pages;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.jdbc.Sql;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.codeforamerica.shiba.pages.DatePart.*;
import static org.codeforamerica.shiba.pages.YesNoAnswer.NO;
import static org.codeforamerica.shiba.pages.YesNoAnswer.YES;
import static org.mockito.Mockito.when;

public class UserJourneyPageTest extends AbstractBasePageTest {

    @MockBean
    Clock clock;

    Page page;

    @Override
    @BeforeEach
    void setUp() throws IOException {
        super.setUp();
        driver.navigate().to(baseUrl + "/pages/landing");
        page = new Page(driver);
        when(clock.instant()).thenReturn(Instant.now());
    }

    @Test
    void userCanCompleteTheNonExpeditedFlow() {
        completeFlowFromLandingPageToDoYouNeedHelpImmediately();

        assertThat(page.getTitle()).isEqualTo("Do you need help immediately?");
    }

    @Test
    void userCanCompleteTheExpeditedFlow() {
        completeFlowFromLandingPageToDoYouNeedHelpImmediately();
        driver.findElement(By.linkText("Yes, I want to see if I qualify")).click();

        Page expeditedIncomePage = page.choose(YES);
        expeditedIncomePage.enterInput("moneyMadeLast30Days", "123");

        Page liquidAssetsPage = expeditedIncomePage.clickPrimaryButton();
        liquidAssetsPage.enterInput("liquidAssets", "1233");

        Page expeditedExpensesPage = liquidAssetsPage.clickPrimaryButton();
        Page expeditedExpensesAmountPage = expeditedExpensesPage.choose(YES);

        expeditedExpensesAmountPage.enterInput("expeditedExpensesAmount", "333");
        Page expeditedUtilityPaymentsPage = expeditedExpensesAmountPage.clickPrimaryButton();

        expeditedUtilityPaymentsPage.selectEnumeratedInput("payForUtilities", "Cooling");
        Page expeditedMigrantFarmWorkerPage = expeditedUtilityPaymentsPage.clickPrimaryButton();

        expeditedMigrantFarmWorkerPage.choose(NO);
    }

    @Test
    void shouldCaptureSubmissionTimeInSuccessPage() {
        when(clock.instant()).thenReturn(LocalDateTime.of(2020, 1, 1, 10, 10).atOffset(ZoneOffset.UTC).toInstant());

        SuccessPage successPage = minimumFlowToSuccessPage();

        assertThat(successPage.getTitle()).isEqualTo("Success");
        assertThat(successPage.getSubmissionTime()).contains("January 1, 2020");
    }

    @Test
    void shouldDownloadPDFWhenClickDownloadMyReceipt() {
        SuccessPage successPage = minimumFlowToSuccessPage();

        successPage.downloadReceipt();
        await().until(() -> path.resolve("DHS-5223.pdf").toFile().exists());
    }

    @Test
    void shouldDownloadXMLWhenClickDownloadXML() {
        SuccessPage successPage = minimumFlowToSuccessPage();

        successPage.downloadXML();
        await().until(() -> path.resolve("ApplyMN.xml").toFile().exists());
    }

    @Test
    @Sql(statements = "TRUNCATE TABLE application_metrics;")
    void shouldCaptureMetricsAfterAnApplicationIsCompleted() {
        when(clock.instant()).thenReturn(
                LocalDateTime.of(2020, 1, 1, 10, 10).atOffset(ZoneOffset.UTC).toInstant(),
                LocalDateTime.of(2020, 1, 1, 10, 15, 30).atOffset(ZoneOffset.UTC).toInstant()
        );
        minimumFlowToSuccessPage();

        driver.navigate().to(baseUrl + "/metrics");
        MetricsPage metricsPage = new MetricsPage(super.driver);
        assertThat(metricsPage.getCardValue("Applications Submitted")).isEqualTo("1");
        assertThat(metricsPage.getCardValue("Completion Time")).contains("05m 30s");
    }

    private Page completeFlowFromLandingPageToDoYouNeedHelpImmediately() {
        Page languagePreferencesPage = page
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
        personalInfoPage.selectEnumeratedInput("sex", "Male");
        personalInfoPage.selectEnumeratedInput("livedInMnWholeLife", "Yes");
        personalInfoPage.enterDateInput("moveToMnDate", MONTH, "02");
        personalInfoPage.enterDateInput("moveToMnDate", DAY, "18");
        personalInfoPage.enterDateInput("moveToMnDate", YEAR, "1776");
        personalInfoPage.enterInput("moveToMnPreviousCity", "Chicago");

        Page contactInfoPage = personalInfoPage.clickPrimaryButton();
        contactInfoPage.enterInput("phoneNumber", "7234567890");
        contactInfoPage.enterInput("email", "some@email.com");
        contactInfoPage.selectEnumeratedInput("phoneOrEmail", "Text me");

        Page homeAddressPage = contactInfoPage
                .clickPrimaryButton()
                .clickSubtleLink()
                .clickSubtleLink();

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

        return mailingAddressPage.clickPrimaryButton();
    }

    private SuccessPage minimumFlowToSuccessPage() {
        completeFlowFromLandingPageToDoYouNeedHelpImmediately();
        driver.findElement(By.linkText("Finish application now")).click();
        Page legalStuffPage = page.clickPrimaryButton();
        legalStuffPage.selectEnumeratedInput("agreeToTerms", "I agree");
        Page signThisApplicationPage = legalStuffPage.clickPrimaryButton();

        signThisApplicationPage.enterInput("applicantSignature", "some name");
        signThisApplicationPage.clickPrimaryButton();

        return new SuccessPage(driver);
    }
}
