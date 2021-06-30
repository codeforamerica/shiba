package org.codeforamerica.shiba.framework;

import org.codeforamerica.shiba.pages.events.PageEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

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
        assertNavigationRedirectsToCorrectNextPage("startPage", "skippableFirstPage/navigation");

        var firstPage = getNextPageAsFormPage("skippableFirstPage");
        assertThat(firstPage.getTitle()).isEqualTo(dummyPageTitle);

        postExpectingSuccessAndAssertRedirectPageNameIsCorrect("firstPage", "input1", "goToSecondPage", "secondPage");
        postExpectingSuccessAndAssertRedirectPageNameIsCorrect("secondPage", "input2", "text2", "endPage");

        var endPage = new FormPage(getPage("endPage"));
        assertThat(endPage.getElementById("iteration0").text()).isEqualTo("goToSecondPage");
    }
}
