package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.AbstractExistingStartTimePageTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {
        "spring.main.allow-bean-definition-overriding=true",
        "pagesConfig=pages-config/test-conditional-rendering.yaml"
})
public class ConditionalRenderingPageTest extends AbstractExistingStartTimePageTest {

    private final String fourthPageTitle = "fourthPageTitle";
    private final String thirdPageTitle = "thirdPageTitle";
    private final String secondPageTitle = "secondPageTitle";
    private final String firstPageTitle = "firstPageTitle";
    private final String eighthPageTitle = "eighthPageTitle";
    private final String pageToSkip = "pageToSkip";
    private final String lastPageTitle = "lastPageTitle";

    @BeforeEach
    protected void setUp() throws IOException {
        super.setUp();
        staticMessageSource.addMessage("starting-page-title", Locale.ENGLISH, "starting page");
        staticMessageSource.addMessage("first-page-title", Locale.ENGLISH, firstPageTitle);
        staticMessageSource.addMessage("second-page-title", Locale.ENGLISH, secondPageTitle);
        staticMessageSource.addMessage("third-page-title", Locale.ENGLISH, thirdPageTitle);
        staticMessageSource.addMessage("fourth-page-title", Locale.ENGLISH, fourthPageTitle);
        staticMessageSource.addMessage("eighth-page-title", Locale.ENGLISH, eighthPageTitle);
        staticMessageSource.addMessage("ninth-page-title", Locale.ENGLISH, "ninthPageTitle");
        staticMessageSource.addMessage("skip-message-key", Locale.ENGLISH, "SKIP PAGE");
        staticMessageSource.addMessage("not-skip-message-key", Locale.ENGLISH, "NOT SKIP PAGE");
        staticMessageSource.addMessage("page-to-skip-title", Locale.ENGLISH, pageToSkip);
        staticMessageSource.addMessage("last-page-title", Locale.ENGLISH, lastPageTitle);
    }

    @Test
    void shouldNotRenderPageAndNavigateToTheNextPageIfTheSkipConditionIsTrue() {
        driver.navigate().to(baseUrl + "/pages/firstPage");
        testPage.enter("someRadioInputName", "SKIP PAGE");
        driver.findElement(By.cssSelector("button")).click();

        assertThat(driver.getTitle()).isEqualTo(thirdPageTitle);
    }

    @Test
    void shouldSupportSkippingMoreThanOnePageInARow() {
        driver.navigate().to(baseUrl + "/pages/firstPage");
        testPage.enter("someRadioInputName", "SKIP PAGE");
        testPage.enter("radioInputToSkipThirdPage", "SKIP PAGE");

        driver.findElement(By.cssSelector("button")).click();
        assertThat(driver.getTitle()).isEqualTo(fourthPageTitle);
    }

    @Test
    void shouldRenderPageIfTheSkipConditionIsFalse() {
        driver.navigate().to(baseUrl + "/pages/firstPage");
        WebElement radioToClick = driver.findElements(By.cssSelector("span")).stream()
                .filter(webElement -> webElement.getText().equals("NOT SKIP PAGE"))
                .findFirst()
                .orElseThrow();
        radioToClick.click();

        driver.findElement(By.cssSelector("button")).click();

        assertThat(driver.getTitle()).isEqualTo(secondPageTitle);
    }

    @Test
    void skipConditionBasedOnPageGroupData() {
        navigateTo("sixthPage");

        testPage.enter("foo", "goToSeventhPage");
        testPage.clickContinue();
        testPage.enter("foo", "SKIP");
        testPage.clickContinue();
        testPage.clickContinue();

        assertThat(driver.getTitle()).isEqualTo(fourthPageTitle);
    }

    @Test
    void shouldSkipGoingBackwardsAsWell() {
        driver.navigate().to(baseUrl + "/pages/firstPage");
        testPage.enter("someRadioInputName", "SKIP PAGE");

        driver.findElement(By.cssSelector("button")).click();

        assertThat(driver.getTitle()).isEqualTo(thirdPageTitle);
        driver.findElement(By.partialLinkText("Back")).click();

        assertThat(driver.getTitle()).isEqualTo(firstPageTitle);
    }

    @Test
    void shouldBeAbleToNavigateBackMoreThanOnePage() {
        driver.navigate().to(baseUrl + "/pages/firstPage");
        driver.findElement(By.cssSelector("button")).click();
        assertThat(driver.getTitle()).isEqualTo(secondPageTitle);

        driver.findElement(By.cssSelector("button")).click();
        assertThat(driver.getTitle()).isEqualTo(thirdPageTitle);

        driver.findElement(By.partialLinkText("Back")).click();
        driver.findElement(By.partialLinkText("Back")).click();
        assertThat(driver.getTitle()).isEqualTo(firstPageTitle);
    }

    @Test
    void shouldRemoveDataForSkippedPage() {
        navigateTo("firstPage");
        testPage.enter("someRadioInputName", "NOT SKIP PAGE");

        testPage.clickContinue();
        testPage.enter("foo", "something");
        testPage.clickContinue();

        navigateTo("firstPage");
        testPage.enter("someRadioInputName", "SKIP PAGE");
        testPage.clickContinue();

        navigateTo("firstPage");
        testPage.enter("someRadioInputName", "NOT SKIP PAGE");
        testPage.clickContinue();

        assertThat(driver.getTitle()).isEqualTo(secondPageTitle);
        assertThat(testPage.getInputValue("foo")).isEmpty();
    }

    @Test
    void shouldNavigateToTheFirstNextPageWhoseConditionIsTrue() {
        navigateTo("fourthPage");

        testPage.enter("foo", "goToFirstPage");
        testPage.clickContinue();

        assertThat(driver.getTitle()).isEqualTo(firstPageTitle);
    }

    @Test
    void shouldGoToFirstNextPageWhoseConditionIsTrue_forSubworkflow() {
        navigateTo("sixthPage");

        testPage.enter("foo", "goToEighthPage");
        testPage.clickContinue();

        assertThat(driver.getTitle()).isEqualTo(eighthPageTitle);
    }

    @Test
    void shouldSupportConditionalRenderingForMultipleConditions() {
        navigateTo("startingPage");
        testPage.enter("randomInput", "someTextInput");
        testPage.enter("anotherInput", "AnotherTextInput");
        testPage.clickContinue();

        assertThat(driver.getTitle()).isEqualTo(lastPageTitle);
    }

    @Test
    void shouldNotSkipIfMultipleConditionsAreNotMet() {
        navigateTo("startingPage");
        testPage.enter("randomInput", "someTextInput");
        testPage.enter("anotherInput", "notCorrectInput");
        testPage.clickContinue();

        assertThat(driver.getTitle()).isEqualTo(pageToSkip);
    }

    @Test
    void shouldSupportConditionalRenderingForMultipleConditionsWithOrOperator() {
        navigateTo("secondStartingPage");
        testPage.enter("randomInput", "someTextInput");
        testPage.enter("anotherInput", "notCorrectInput");
        testPage.clickContinue();

        assertThat(driver.getTitle()).isEqualTo(lastPageTitle);
    }

    @Test
    void shouldNotSkipIfMultipleConditionsAreNotMetWithOrOperator() {
        navigateTo("secondStartingPage");
        testPage.enter("randomInput", "notCorrectInput");
        testPage.enter("anotherInput", "alsoNotCorrectInput");
        testPage.clickContinue();

        assertThat(driver.getTitle()).isEqualTo(pageToSkip);
    }
}
