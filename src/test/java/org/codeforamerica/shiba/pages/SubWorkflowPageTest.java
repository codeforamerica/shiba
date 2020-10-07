package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.YamlPropertySourceFactory;
import org.codeforamerica.shiba.pages.config.ApplicationConfiguration;
import org.codeforamerica.shiba.pages.events.PageEventPublisher;
import org.codeforamerica.shiba.pages.events.SubworkflowCompletedEvent;
import org.codeforamerica.shiba.pages.events.SubworkflowIterationDeletedEvent;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

public class SubWorkflowPageTest extends AbstractExistingStartTimePageTest {
    @TestConfiguration
    @PropertySource(value = "classpath:pages-config/test-sub-workflow.yaml", factory = YamlPropertySourceFactory.class)
    static class TestPageConfiguration {
        @Bean
        @ConfigurationProperties(prefix = "shiba-configuration-sub-workflow")
        public ApplicationConfiguration applicationConfiguration() {
            return new ApplicationConfiguration();
        }
    }

    @MockBean
    PageEventPublisher pageEventPublisher;

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
    void shouldSupportSoloPageSubworkflow() {
        navigateTo("soloPage");
        String sessionId = driver.manage().getCookieNamed("JSESSIONID").getValue();
        Page endPage = testPage.clickPrimaryButton();
        verify(pageEventPublisher).publish(new SubworkflowCompletedEvent(sessionId, "group2"));
    }

    @Test
    void shouldDeleteLastSubworkflowAndRedirectBackIfNoDataRedirectPageIsNotPresent() {
        this.staticMessageSource.addMessage("end-page-title", Locale.US, "end-page-title");
        navigateTo("soloPage");
        String sessionId = driver.manage().getCookieNamed("JSESSIONID").getValue();
        Page endSoloPage = testPage.clickPrimaryButton();
        driver.findElement(By.id("iteration0-delete")).click();
        assertThat(driver.getTitle()).isEqualTo("end-page-title");
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
    void shouldPublishSubflowCompletedEventAnyOfTheConfiguredCompletePages() {
        navigateTo("startPage");
        Page firstPage = testPage.clickPrimaryButton();
        firstPage.enterInput("input1", "goToThirdPage");
        Page thirdPage = testPage.clickPrimaryButton();
        thirdPage.enterInput("input3", "text 3");
        String sessionId = driver.manage().getCookieNamed("JSESSIONID").getValue();
        testPage.clickPrimaryButton();

        verify(pageEventPublisher).publish(new SubworkflowCompletedEvent(sessionId, "group1"));
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
        testPage.clickPrimaryButton();

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
    void shouldPublishSubflowIterationDeleted() {
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

        String sessionId = driver.manage().getCookieNamed("JSESSIONID").getValue();
        driver.findElement(By.id("iteration0-delete")).click();
        verify(pageEventPublisher).publish(new SubworkflowIterationDeletedEvent(sessionId, "group1"));
    }

    @Test
    void shouldPublishSubflowIterationDeletedOnGroupDelete() {
        navigateTo("startPage");
        Page firstPage = testPage.clickPrimaryButton();
        firstPage.enterInput("input1", "goToSecondPage");
        Page secondPage = testPage.clickPrimaryButton();
        secondPage.enterInput("input2", "text 2");
        testPage.clickPrimaryButton();

        String sessionId = driver.manage().getCookieNamed("JSESSIONID").getValue();
        driver.findElement(By.id("iteration0-delete")).click();
        driver.findElement(By.tagName("button")).click();

        verify(pageEventPublisher).publish(new SubworkflowIterationDeletedEvent(sessionId, "group1"));
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
        testPage.clickPrimaryButton();

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

    @Test
    void shouldRedirectWhenPageDoesntHaveNecessaryDatasources() {
        this.staticMessageSource.addMessage("earlier-page-title", Locale.US, "earlierPage");
        navigateTo("startPage");
        Page firstPage = testPage.clickPrimaryButton();
        firstPage.enterInput("input1", "goToSecondPage");
        Page secondPage = testPage.clickPrimaryButton();
        secondPage.enterInput("input2", "text 2");
        testPage.clickPrimaryButton();

        driver.findElement(By.id("iteration0-delete")).click();
        driver.findElement(By.tagName("button")).click();

        testPage.goBack();

        assertThat(testPage.getTitle()).isEqualTo("earlierPage");
    }
}
