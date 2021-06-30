package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.AbstractExistingStartTimePageTest;
import org.codeforamerica.shiba.pages.events.PageEventPublisher;
import org.codeforamerica.shiba.pages.events.SubworkflowCompletedEvent;
import org.codeforamerica.shiba.pages.events.SubworkflowIterationDeletedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {"pagesConfig=pages-config/test-sub-workflow.yaml"})
@ActiveProfiles("test")
public class SubWorkflowPageTest extends AbstractExistingStartTimePageTest {

    @MockBean
    PageEventPublisher pageEventPublisher;

    @Override
    @BeforeEach
    protected void setUp() throws IOException {
        super.setUp();
        staticMessageSource.addMessage("start-page-title", Locale.ENGLISH, "start-page-title");
        staticMessageSource.addMessage("end-page-title", Locale.ENGLISH, "end-page-title");
        staticMessageSource.addMessage("solo-page-title", Locale.ENGLISH, "solo-page-title");
        staticMessageSource.addMessage("warning-page-header", Locale.ENGLISH, "This is a warning for: {0}");
    }

    @Test
    void shouldPublishSubflowIterationDeleted() {
        String warningPageTitle = "warning page title";
        staticMessageSource.addMessage("some-warning-title", Locale.ENGLISH, warningPageTitle);

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
        testPage.clickButton("Yes, remove it");
        verify(pageEventPublisher).publish(any(SubworkflowIterationDeletedEvent.class));
    }

    @Test
    void shouldGoToSpecifiedPageWhenGoBackFromEndOfTheWorkflow() {
        String redirectPageTitle = "some title";
        staticMessageSource.addMessage("some-redirect-title", Locale.ENGLISH, redirectPageTitle);

        navigateTo("startPage");
        testPage.clickContinue();
        testPage.enter("input1", "goToSecondPage");
        testPage.clickContinue();
        testPage.enter("input2", "text 2");
        testPage.clickContinue();
        testPage.goBack();

        assertThat(testPage.getTitle()).isEqualTo(redirectPageTitle);
    }

    @Test
    void shouldGoToSpecifiedPageWhenAttemptToDeleteAnyDataEntry() {
        String warningPageTitle = "some title";
        staticMessageSource.addMessage("some-warning-title", Locale.ENGLISH, warningPageTitle);
        String endPageTitle = "some other title";
        staticMessageSource.addMessage("some-other-title", Locale.ENGLISH, endPageTitle);
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
        staticMessageSource.addMessage("earlier-page-title", Locale.ENGLISH, "earlierPage");
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
