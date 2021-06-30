package org.codeforamerica.shiba.framework;

import org.codeforamerica.shiba.pages.events.PageEventPublisher;
import org.codeforamerica.shiba.pages.events.SubworkflowCompletedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@SpringBootTest(properties = {"pagesConfig=pages-config/test-sub-workflow.yaml"})
public class SubworkflowTest extends AbstractFrameworkTest {
    @MockBean
    private PageEventPublisher pageEventPublisher;

    @Override
    @BeforeEach
    protected void setUp() throws Exception {
        super.setUp();
        staticMessageSource.addMessage("start-page-title", Locale.ENGLISH, "start-page-title");
        staticMessageSource.addMessage("end-page-title", Locale.ENGLISH, "end-page-title");
        staticMessageSource.addMessage("solo-page-title", Locale.ENGLISH, "solo-page-title");
        staticMessageSource.addMessage("warning-page-header", Locale.ENGLISH, "This is a warning for: {0}");
    }

    @Test
    void shouldDisplayInputFromSubflowInFinalPage() throws Exception {
        postExpectingSuccess("startPage");
        // skippableFirstPage should be skipped due to skip condition
        assertNavigationRedirectsToCorrectNextPage("startPage", "skippableFirstPage/navigation");

        var firstPage = getNextPageAsFormPage("skippableFirstPage");
        assertThat(firstPage.getTitle()).isEqualTo(dummyPageTitle);

        postExpectingSuccessAndAssertRedirectPageNameIsCorrect("firstPage",
                                                               "input1",
                                                               "goToSecondPage",
                                                               "secondPage");
        postExpectingSuccessAndAssertRedirectPageNameIsCorrect("secondPage",
                                                               "input2",
                                                               "text2",
                                                               "endPage");

        assertReviewPageDisplaysCorrectInputValueForFirstIteration("goToSecondPage");
    }

    @Test
    void shouldSupportSkippableFirstPage() throws Exception {
        postExpectingSuccessAndAssertRedirectPageNameIsCorrect("startPage",
                                                               "foo",
                                                               "someinput",
                                                               "skippableFirstPage");
        postExpectingSuccessAndAssertRedirectPageNameIsCorrect("skippableFirstPage",
                                                               "inputSkippable",
                                                               "bar",
                                                               "firstPage");
        postExpectingSuccessAndAssertRedirectPageNameIsCorrect("firstPage",
                                                               "input1",
                                                               "goToSecondPage",
                                                               "secondPage");
        postExpectingSuccessAndAssertRedirectPageNameIsCorrect("secondPage",
                                                               "input2",
                                                               "text 2",
                                                               "endPage");

        assertReviewPageDisplaysCorrectInputValueForFirstIteration("goToSecondPage");
    }

    @Test
    void shouldSupportSoloPageSubworkflow() throws Exception {
        postExpectingSuccess("soloPage");
        verify(pageEventPublisher).publish(any(SubworkflowCompletedEvent.class));
    }

    @Test
    void shouldCompleteSubflowInAnyOfTheConfiguredCompletePages() throws Exception {
        postExpectingSuccess("startPage");
        assertNavigationRedirectsToCorrectNextPage("startPage", "skippableFirstPage/navigation");

        var firstPage = getNextPageAsFormPage("skippableFirstPage");
        assertThat(firstPage.getTitle()).isEqualTo(dummyPageTitle);

        postExpectingSuccessAndAssertRedirectPageNameIsCorrect("firstPage",
                                                               "input1",
                                                               "goToThirdPage",
                                                               "thirdPage");
        postExpectingSuccessAndAssertRedirectPageNameIsCorrect("thirdPage",
                                                               "input3",
                                                               "text 3",
                                                               "endPage");
        assertReviewPageDisplaysCorrectInputValueForFirstIteration("goToThirdPage");
        verify(pageEventPublisher).publish(any(SubworkflowCompletedEvent.class));
    }

    @Test
    void shouldNotDisplayIterationInEndPageIfIterationWasNotCompleted() throws Exception {
        // Complete an iteration
        postExpectingSuccess("startPage");
        assertNavigationRedirectsToCorrectNextPage("startPage", "skippableFirstPage/navigation");
        postExpectingSuccessAndAssertRedirectPageNameIsCorrect("skippableFirstPage",
                                                               "inputSkippable",
                                                               "bar",
                                                               "firstPage");
        postExpectingSuccessAndAssertRedirectPageNameIsCorrect("firstPage",
                                                               "input1",
                                                               "goToSecondPage",
                                                               "secondPage");
        postExpectingSuccessAndAssertRedirectPageNameIsCorrect("secondPage",
                                                               "input2",
                                                               "text 2",
                                                               "endPage");
        assertReviewPageDisplaysCorrectInputValueForFirstIteration("goToSecondPage");


        // Start another iteration but don't finish it
        postExpectingSuccessAndAssertRedirectPageNameIsCorrect("firstPage",
                                                               "input1",
                                                               "goToThirdPage",
                                                               "thirdPage");

        var endPage = new FormPage(getPage("endPage"));
        assertThat(endPage.getElementById("iteration0")).isNotNull();
        assertThat(endPage.getElementById("iteration0").text()).isEqualTo("goToSecondPage");
        assertThat(endPage.getElementById("iteration1")).isNull();
    }

    /**
     * endPage is a custom review page which displays the the value that was entered for input1 on firstPage
     * <p>
     * This method asserts that the expected input value is shown on the review page
     */
    private void assertReviewPageDisplaysCorrectInputValueForFirstIteration(
            String expectedFirstPageInput1Value) throws Exception {
        var endPage = new FormPage(getPage("endPage"));
        assertThat(endPage.getElementById("iteration0").text()).isEqualTo(expectedFirstPageInput1Value);
    }
}
