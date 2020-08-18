package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.YamlPropertySourceFactory;
import org.codeforamerica.shiba.pages.config.ApplicationConfiguration;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

public class PageSubWorkflowPageTest extends AbstractStaticMessageSourcePageTest {
    @TestConfiguration
    @PropertySource(value = "classpath:pages-config/test-sub-workflow.yaml", factory = YamlPropertySourceFactory.class)
    static class TestPageConfiguration extends MetricsTestConfigurationWithExistingStartTime {
        @Bean
        @ConfigurationProperties(prefix = "shiba-configuration-sub-workflow")
        public ApplicationConfiguration applicationConfiguration() {
            return new ApplicationConfiguration();
        }
    }

    @Test
    void shouldDisplayInputFromSubflowInFinalPage() {
        navigateTo("startPage");
        Page firstPage = testPage.clickPrimaryButton();
        firstPage.enterInput("input1", "goToSecondPage");
        Page secondPage = testPage.clickPrimaryButton();
        secondPage.enterInput("input2", "text 2");
        Page endPage = testPage.clickPrimaryButton();

        assertThat(driver.findElement(By.id("iteration0")).getText()).isEqualTo("goToSecondPage");
    }

    @Test
    void shouldCompleteSubflowInAnyOfTheConfiguredCompletePages() {
        navigateTo("startPage");
        Page firstPage = testPage.clickPrimaryButton();
        firstPage.enterInput("input1", "goToThirdPage");
        Page thirdPage = testPage.clickPrimaryButton();
        thirdPage.enterInput("input3", "text 3");
        Page endPage = testPage.clickPrimaryButton();

        assertThat(driver.findElement(By.id("iteration0")).getText()).isEqualTo("goToThirdPage");
    }

    @Test
    void shouldNotDisplayIterationInEndPageIfIterationWasNotCompleted() {
        navigateTo("startPage");
        Page firstPage = testPage.clickPrimaryButton();
        firstPage.enterInput("input1", "goToSecondPage");
        Page secondPage = testPage.clickPrimaryButton();
        secondPage.enterInput("input2", "text 2");
        Page endPage = testPage.clickPrimaryButton();
        assertThat(driver.findElement(By.id("iteration0")).getText()).isEqualTo("goToSecondPage");

        Page firstPage1 = endPage.clickPrimaryButton();
        firstPage.enterInput("input1", "goToThirdPage");
        Page secondPage1 = testPage.clickPrimaryButton();
        secondPage1.goBack();
        firstPage1.goBack();

        assertThat(driver.findElement(By.id("iteration0")).getText()).isEqualTo("goToSecondPage");
    }

    @Test
    void shouldNotDisplayDataFromPastIterationsWhenStartingANewSubworkflow() {
        navigateTo("startPage");
        Page firstPage = testPage.clickPrimaryButton();
        firstPage.enterInput("input1", "goToSecondPage");
        Page secondPage = testPage.clickPrimaryButton();
        secondPage.enterInput("input2", "text 2");
        Page endPage = testPage.clickPrimaryButton();
        Page firstPage1 = endPage.clickPrimaryButton();

        assertThat(firstPage1.getInputValue("input1")).isEmpty();
    }

    @Test
    void shouldDisplayInputFromAllCompletedIterations() {
        navigateTo("startPage");
        Page firstPage = testPage.clickPrimaryButton();
        firstPage.enterInput("input1", "goToSecondPage");
        Page secondPage = testPage.clickPrimaryButton();
        secondPage.enterInput("input2", "text 2");
        Page endPage = testPage.clickPrimaryButton();

        firstPage = endPage.clickPrimaryButton();
        firstPage.enterInput("input1", "goToThirdPage");
        secondPage = testPage.clickPrimaryButton();
        secondPage.enterInput("input3", "text 3");
        endPage = testPage.clickPrimaryButton();

        assertThat(driver.findElement(By.id("iteration0")).getText()).isEqualTo("goToSecondPage");
        assertThat(driver.findElement(By.id("iteration1")).getText()).isEqualTo("goToThirdPage");
    }

    @Test
    void shouldRemoveTheEntryFromFinalPageIfDeleted() {
        navigateTo("startPage");
        Page firstPage = testPage.clickPrimaryButton();
        firstPage.enterInput("input1", "goToSecondPage");
        Page secondPage = testPage.clickPrimaryButton();
        secondPage.enterInput("input2", "text 2");
        Page endPage = testPage.clickPrimaryButton();

        firstPage = endPage.clickPrimaryButton();
        firstPage.enterInput("input1", "goToThirdPage");
        secondPage = testPage.clickPrimaryButton();
        secondPage.enterInput("input3", "text 4");
        testPage.clickPrimaryButton();

        driver.findElement(By.id("iteration0-delete")).click();
        assertThat(driver.findElement(By.id("iteration0")).getText()).isEqualTo("goToThirdPage");
    }

    @Test
    void shouldGoToSpecifiedPageWhenGoBackFromEndOfTheWorkflow() {
        String warningPageTitle = "some title";
        this.staticMessageSource.addMessage("some-warning-title", Locale.US, warningPageTitle);

        navigateTo("startPage");
        Page firstPage = testPage.clickPrimaryButton();
        firstPage.enterInput("input1", "goToSecondPage");
        Page secondPage = testPage.clickPrimaryButton();
        secondPage.enterInput("input2", "text 2");
        testPage.clickPrimaryButton();

        testPage.goBack();

        assertThat(testPage.getTitle()).isEqualTo(warningPageTitle);
    }

    @Test
    void shouldGoToSpecifiedPageWhenAttemptToDeleteTheLastDataEntry() {
        String warningPageTitle = "some title";
        this.staticMessageSource.addMessage("some-warning-title", Locale.US, warningPageTitle);
        String endPageTitle = "some other title";
        this.staticMessageSource.addMessage("some-other-title", Locale.US, endPageTitle);
        navigateTo("startPage");
        Page firstPage = testPage.clickPrimaryButton();
        firstPage.enterInput("input1", "goToSecondPage");
        Page secondPage = testPage.clickPrimaryButton();
        secondPage.enterInput("input2", "text 2");
        Page endPage = testPage.clickPrimaryButton();

        driver.findElement(By.id("iteration0-delete")).click();
        assertThat(driver.getTitle()).isEqualTo(warningPageTitle);
    }

    @Test
    void shouldClearOutSubworkflowsWhenChoosingToRestartSubworkflow() {
        navigateTo("startPage");
        Page firstPage = testPage.clickPrimaryButton();
        firstPage.enterInput("input1", "goToSecondPage");
        Page secondPage = testPage.clickPrimaryButton();
        secondPage.enterInput("input2", "text 2");
        testPage.clickPrimaryButton();
        testPage.goBack();

        driver.findElement(By.tagName("button")).click();

        firstPage = testPage.clickPrimaryButton();
        firstPage.enterInput("input1", "goToThirdPage");
        secondPage = testPage.clickPrimaryButton();
        secondPage.enterInput("input3", "new text 2");
        testPage.clickPrimaryButton();

        assertThat(driver.findElement(By.id("iteration0")).getText()).isEqualTo("goToThirdPage");
    }
}
