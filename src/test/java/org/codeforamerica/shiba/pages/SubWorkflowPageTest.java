package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.AbstractExistingStartTimePageTest;
import org.codeforamerica.shiba.YamlPropertySourceFactory;
import org.codeforamerica.shiba.pages.config.ApplicationConfiguration;
import org.codeforamerica.shiba.pages.events.PageEventPublisher;
import org.codeforamerica.shiba.pages.events.SubworkflowCompletedEvent;
import org.codeforamerica.shiba.pages.events.SubworkflowIterationDeletedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;

import java.io.IOException;
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

    @Override
    @BeforeEach
    protected void setUp() throws IOException {
        super.setUp();
        staticMessageSource.addMessage("start-page-title", Locale.US, "start-page-title");
        staticMessageSource.addMessage("end-page-title", Locale.US, "end-page-title");
        staticMessageSource.addMessage("solo-page-title", Locale.US, "solo-page-title");
    }

    @Test
    void shouldDisplayInputFromSubflowInFinalPage() {
        navigateTo("startPage");
        testPage.clickContinue();
        testPage.enter("input1", "goToSecondPage");
        testPage.clickContinue();
        testPage.enter("input2", "text 2");
        testPage.clickContinue();

        assertThat(driver.findElement(By.id("iteration0")).getText()).isEqualTo("goToSecondPage");
    }

    @Test
    void shouldSupportSoloPageSubworkflow() {
        navigateTo("soloPage");
        String sessionId = driver.manage().getCookieNamed("JSESSIONID").getValue();
        testPage.clickContinue();
        verify(pageEventPublisher).publish(new SubworkflowCompletedEvent(sessionId, "group2"));
    }

    @Test
    void shouldDeleteLastSubworkflowAndRedirectBackIfNoDataRedirectPageIsNotPresent() {
        navigateTo("soloPage");
        testPage.clickContinue();
        driver.findElement(By.id("iteration0-delete")).click();
        assertThat(driver.getTitle()).isEqualTo("end-page-title");
    }

    @Test
    void shouldCompleteSubflowInAnyOfTheConfiguredCompletePages() {
        navigateTo("startPage");
        testPage.clickContinue();
        testPage.enter("input1", "goToThirdPage");
        testPage.clickContinue();
        testPage.enter("input3", "text 3");
        testPage.clickContinue();

        assertThat(driver.findElement(By.id("iteration0")).getText()).isEqualTo("goToThirdPage");
    }

    @Test
    void shouldPublishSubflowCompletedEventAnyOfTheConfiguredCompletePages() {
        navigateTo("startPage");
        testPage.clickContinue();
        testPage.enter("input1", "goToThirdPage");
        testPage.clickContinue();
        testPage.enter("input3", "text 3");
        String sessionId = driver.manage().getCookieNamed("JSESSIONID").getValue();
        testPage.clickContinue();

        verify(pageEventPublisher).publish(new SubworkflowCompletedEvent(sessionId, "group1"));
    }

    @Test
    void shouldNotDisplayIterationInEndPageIfIterationWasNotCompleted() {
        navigateTo("startPage");
        testPage.clickContinue();
        testPage.enter("input1", "goToSecondPage");
        testPage.clickContinue();
        testPage.enter("input2", "text 2");
        testPage.clickContinue();
        assertThat(driver.findElement(By.id("iteration0")).getText()).isEqualTo("goToSecondPage");

        testPage.clickContinue();
        testPage.enter("input1", "goToThirdPage");
        testPage.clickContinue();
        testPage.goBack();
        testPage.goBack();

        assertThat(driver.findElement(By.id("iteration0")).getText()).isEqualTo("goToSecondPage");
    }

    @Test
    void shouldNotDisplayDataFromPastIterationsWhenStartingANewSubworkflow() {
        navigateTo("startPage");
        testPage.clickContinue();
        testPage.enter("input1", "goToSecondPage");
        testPage.clickContinue();
        testPage.enter("input2", "text 2");
        testPage.clickContinue();
        testPage.clickContinue();

        assertThat(testPage.getInputValue("input1")).isEmpty();
    }

    @Test
    void shouldDisplayInputFromAllCompletedIterations() {
        navigateTo("startPage");
        testPage.clickContinue();
        testPage.enter("input1", "goToSecondPage");
        testPage.clickContinue();
        testPage.enter("input2", "text 2");
        testPage.clickContinue();

        testPage.clickContinue();
        testPage.enter("input1", "goToThirdPage");
        testPage.clickContinue();
        testPage.enter("input3", "text 3");
        testPage.clickContinue();

        assertThat(driver.findElement(By.id("iteration0")).getText()).isEqualTo("goToSecondPage");
        assertThat(driver.findElement(By.id("iteration1")).getText()).isEqualTo("goToThirdPage");
    }

    @Test
    void shouldRemoveTheEntryFromFinalPageIfDeleted() {
        navigateTo("startPage");
        testPage.clickContinue();
        testPage.enter("input1", "goToSecondPage");
        testPage.clickContinue();
        testPage.enter("input2", "text 2");
        testPage.clickContinue();

        testPage.clickContinue();
        testPage.enter("input1", "goToThirdPage");
        testPage.clickContinue();
        testPage.enter("input3", "text 4");
        testPage.clickContinue();

        driver.findElement(By.id("iteration0-delete")).click();
        assertThat(driver.findElement(By.id("iteration0")).getText()).isEqualTo("goToThirdPage");
    }

    @Test
    void shouldPublishSubflowIterationDeleted() {
        navigateTo("startPage");
        testPage.clickContinue();
        testPage.enter("input1", "goToSecondPage");
        testPage.clickContinue();
        testPage.enter("input2", "text 2");
        testPage.clickContinue();

        testPage.clickContinue();
        testPage.enter("input1", "goToThirdPage");
        testPage.clickContinue();
        testPage.enter("input3", "text 4");
        testPage.clickContinue();

        String sessionId = driver.manage().getCookieNamed("JSESSIONID").getValue();
        driver.findElement(By.id("iteration0-delete")).click();
        verify(pageEventPublisher).publish(new SubworkflowIterationDeletedEvent(sessionId, "group1"));
    }

    @Test
    void shouldPublishSubflowIterationDeletedOnGroupDelete() {
        navigateTo("startPage");
        testPage.clickContinue();
        testPage.enter("input1", "goToSecondPage");
        testPage.clickContinue();
        testPage.enter("input2", "text 2");
        testPage.clickContinue();

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
        testPage.clickContinue();
        testPage.enter("input1", "goToSecondPage");
        testPage.clickContinue();
        testPage.enter("input2", "text 2");
        testPage.clickContinue();

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
        testPage.clickContinue();
        testPage.enter("input1", "goToSecondPage");
        testPage.clickContinue();
        testPage.enter("input2", "text 2");
        testPage.clickContinue();

        driver.findElement(By.id("iteration0-delete")).click();
        assertThat(driver.getTitle()).isEqualTo(warningPageTitle);
    }

    @Test
    void shouldClearOutSubworkflowsWhenChoosingToRestartSubworkflow() {
        navigateTo("startPage");
        testPage.clickContinue();
        testPage.enter("input1", "goToSecondPage");
        testPage.clickContinue();
        testPage.enter("input2", "text 2");
        testPage.clickContinue();
        testPage.goBack();

        driver.findElement(By.tagName("button")).click();

        testPage.clickContinue();
        testPage.enter("input1", "goToThirdPage");
        testPage.clickContinue();
        testPage.enter("input3", "new text 2");
        testPage.clickContinue();

        assertThat(driver.findElement(By.id("iteration0")).getText()).isEqualTo("goToThirdPage");
    }

    @Test
    void shouldRedirectWhenPageDoesntHaveNecessaryDatasources() {
        this.staticMessageSource.addMessage("earlier-page-title", Locale.US, "earlierPage");
        navigateTo("startPage");
        testPage.clickContinue();
        testPage.enter("input1", "goToSecondPage");
        testPage.clickContinue();
        testPage.enter("input2", "text 2");
        testPage.clickContinue();

        driver.findElement(By.id("iteration0-delete")).click();
        driver.findElement(By.tagName("button")).click();

        testPage.goBack();

        assertThat(testPage.getTitle()).isEqualTo("earlierPage");
    }
}
