package org.codeforamerica.shiba.pages;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.openqa.selenium.By;

import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

public class ExpeditedServiceDeterminationPageTest extends AbstractStaticMessageSourcePageTest {
    private static final String noneOfTheAbove = "None of the above";
    private static final String deniedTitle = "denied title";
    private static final String qualifiedTitle = "qualified title";
    private static final String qualifiedHeader = "expedited header";
    private static final String deniedHeader = "denied header";
    private static final String qualifiedDetail = "qualified detail";
    private static final String deniedDetail = "denied detail";
    private static final String heating = "heating";
    private static final String cooling = "cooling";
    private static final String electricity = "electricity";
    private static final String phone = "phone";

    @BeforeEach
    void setup() {
        staticMessageSource.addMessage("expedited-utility-payments.none-of-the-above", Locale.US, noneOfTheAbove);
        staticMessageSource.addMessage("expedited-determination.denied-title", Locale.US, deniedTitle);
        staticMessageSource.addMessage("expedited-determination.qualified-title", Locale.US, qualifiedTitle);
        staticMessageSource.addMessage("expedited-determination.qualified-header", Locale.US, qualifiedHeader);
        staticMessageSource.addMessage("expedited-determination.denied-header", Locale.US, deniedHeader);
        staticMessageSource.addMessage("expedited-determination.qualified-detail", Locale.US, qualifiedDetail);
        staticMessageSource.addMessage("expedited-determination.denied-detail", Locale.US, deniedDetail);
        staticMessageSource.addMessage("expedited-utility-payments.heating", Locale.US, heating);
        staticMessageSource.addMessage("expedited-utility-payments.cooling", Locale.US, cooling);
        staticMessageSource.addMessage("expedited-utility-payments.electricity", Locale.US, electricity);
        staticMessageSource.addMessage("expedited-utility-payments.phone", Locale.US, phone);

        driver.navigate().to(baseUrl + "/pages/languagePreferences");
    }

    @ParameterizedTest
    @CsvSource(value = {
            "149,100,true",
            "150,100,false",
            "149,101,false",
    })
    void shouldQualifyWhenMeetingIncomeAndAssetsThresholds(String income,
                                                           String assets,
                                                           boolean isQualified) {
        driver.navigate().to(baseUrl + "/pages/doYouLiveAloneExpedited");

        testPage.choose(YesNoAnswer.YES);

        testPage.enterInput("moneyMadeLast30Days", income);
        testPage.clickPrimaryButton();

        testPage.enterInput("liquidAssets", assets);
        testPage.clickPrimaryButton();

        testPage.choose(YesNoAnswer.NO);

        testPage.selectEnumeratedInput("payForUtilities", noneOfTheAbove);
        testPage.clickPrimaryButton();

        testPage.choose(YesNoAnswer.NO);

        assertThat(driver.getTitle()).isEqualTo(isQualified ? qualifiedTitle : deniedTitle);
        assertThat(driver.findElement(By.cssSelector("h2")).getText()).isEqualTo(isQualified ? qualifiedHeader : deniedHeader);
        assertThat(driver.findElement(By.cssSelector("h2 ~ p")).getText()).isEqualTo(isQualified ? qualifiedDetail : deniedDetail);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "100,YES,true",
            "101,YES,false",
            "100,NO,false"
    })
    void shouldOnlyQualifyWhenApplicantIsMigrantWorkerAndMeetAssetThreshold(
            String assets,
            YesNoAnswer isMigrantWorker,
            boolean isQualified) {
        driver.navigate().to(baseUrl + "/pages/doYouLiveAloneExpedited");

        testPage.choose(YesNoAnswer.YES);

        testPage.enterInput("moneyMadeLast30Days", "200");
        testPage.clickPrimaryButton();

        testPage.enterInput("liquidAssets", assets);
        testPage.clickPrimaryButton();

        testPage.choose(YesNoAnswer.NO);

        testPage.selectEnumeratedInput("payForUtilities", noneOfTheAbove);
        testPage.clickPrimaryButton();

        testPage.choose(isMigrantWorker);

        assertThat(driver.getTitle()).isEqualTo(isQualified ? qualifiedTitle : deniedTitle);
        assertThat(driver.findElement(By.cssSelector("h2")).getText()).isEqualTo(isQualified ? qualifiedHeader : deniedHeader);
        assertThat(driver.findElement(By.cssSelector("h2 ~ p")).getText()).isEqualTo(isQualified ? qualifiedDetail : deniedDetail);
    }

    @ParameterizedTest
    @MethodSource
    void shouldQualifyWhenMeetingIncomeAndAssetsVersusCostsCriteria(String income,
                                                                    String assets,
                                                                    String rentMortgageAmount,
                                                                    List<String> utilityOptions,
                                                                    boolean isQualified) {
        driver.navigate().to(baseUrl + "/pages/doYouLiveAloneExpedited");

        testPage.choose(YesNoAnswer.YES);

        testPage.enterInput("moneyMadeLast30Days", income);
        testPage.clickPrimaryButton();

        testPage.enterInput("liquidAssets", assets);
        testPage.clickPrimaryButton();

        testPage.choose(YesNoAnswer.YES);

        testPage.enterInput("expeditedExpensesAmount", rentMortgageAmount);
        testPage.clickPrimaryButton();

        utilityOptions.forEach(option -> testPage.selectEnumeratedInput("payForUtilities", option));
        testPage.clickPrimaryButton();

        testPage.choose(YesNoAnswer.NO);

        assertThat(driver.getTitle()).isEqualTo(isQualified ? qualifiedTitle : deniedTitle);
        assertThat(driver.findElement(By.cssSelector("h2")).getText()).isEqualTo(isQualified ? qualifiedHeader : deniedHeader);
        assertThat(driver.findElement(By.cssSelector("h2 ~ p")).getText()).isEqualTo(isQualified ? qualifiedDetail : deniedDetail);
    }

    @SuppressWarnings("unused")
    static List<Arguments> shouldQualifyWhenMeetingIncomeAndAssetsVersusCostsCriteria() {
        return List.of(
                Arguments.of("300", "300", "111", List.of(heating), true),
                Arguments.of("300", "300", "111", List.of(heating, electricity, phone), true),
                Arguments.of("300", "300", "111", List.of(cooling), true),
                Arguments.of("300", "300", "111", List.of(cooling, electricity, phone), true),
                Arguments.of("300", "300", "111", List.of(heating, cooling), true),
                Arguments.of("300", "300", "111", List.of(heating, cooling, electricity, phone), true),
                Arguments.of("300", "300", "110", List.of(heating), false),
                Arguments.of("300", "300", "110", List.of(cooling), false),
                Arguments.of("300", "300", "110", List.of(heating, cooling), false),
                Arguments.of("300", "300", "458", List.of(electricity), true),
                Arguments.of("300", "300", "457", List.of(electricity), false),
                Arguments.of("300", "300", "552", List.of(phone), true),
                Arguments.of("300", "300", "551", List.of(phone), false),
                Arguments.of("300", "300", "409", List.of(electricity, phone), true),
                Arguments.of("300", "300", "408", List.of(electricity, phone), false),
                Arguments.of("300", "300", "601", List.of(noneOfTheAbove), true),
                Arguments.of("300", "300", "600", List.of(noneOfTheAbove), false),
                Arguments.of("", "300", "301", List.of(noneOfTheAbove), true),
                Arguments.of("300", "", "301", List.of(noneOfTheAbove), true),
                Arguments.of("200", "289", "", List.of(heating), true)
        );
    }

    @Test
    void shouldCalculateCorrectlyWhenSelectNoForPayingForHousing() {
        driver.navigate().to(baseUrl + "/pages/doYouLiveAloneExpedited");

        testPage.choose(YesNoAnswer.YES);

        testPage.enterInput("moneyMadeLast30Days", "200");
        testPage.clickPrimaryButton();

        testPage.enterInput("liquidAssets", "200");
        testPage.clickPrimaryButton();

        testPage.choose(YesNoAnswer.NO);

        testPage.selectEnumeratedInput("payForUtilities", heating);
        testPage.clickPrimaryButton();

        testPage.choose(YesNoAnswer.NO);

        assertThat(driver.getTitle()).isEqualTo(qualifiedTitle);
        assertThat(driver.findElement(By.cssSelector("h2")).getText()).isEqualTo(qualifiedHeader);
        assertThat(driver.findElement(By.cssSelector("h2 ~ p")).getText()).isEqualTo(qualifiedDetail);
    }
}
