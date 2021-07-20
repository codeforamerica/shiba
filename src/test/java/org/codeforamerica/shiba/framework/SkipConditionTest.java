package org.codeforamerica.shiba.framework;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static java.util.Locale.ENGLISH;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {"pagesConfig=pages-config/test-conditional-rendering.yaml"})
public class SkipConditionTest extends AbstractFrameworkTest {
    private final String fourthPageTitle = "fourthPageTitle";
    private final String thirdPageTitle = "thirdPageTitle";
    private final String secondPageTitle = "secondPageTitle";
    private final String firstPageTitle = "firstPageTitle";
    private final String eighthPageTitle = "eighthPageTitle";
    private final String pageToSkip = "pageToSkip";
    private final String lastPageTitle = "lastPageTitle";

    @BeforeEach
    protected void setUp() throws Exception {
        super.setUp();
        staticMessageSource.addMessage("starting-page-title", ENGLISH, "starting page");
        staticMessageSource.addMessage("first-page-title", ENGLISH, firstPageTitle);
        staticMessageSource.addMessage("second-page-title", ENGLISH, secondPageTitle);
        staticMessageSource.addMessage("third-page-title", ENGLISH, thirdPageTitle);
        staticMessageSource.addMessage("fourth-page-title", ENGLISH, fourthPageTitle);
        staticMessageSource.addMessage("eighth-page-title", ENGLISH, eighthPageTitle);
        staticMessageSource.addMessage("ninth-page-title", ENGLISH, "ninthPageTitle");
        staticMessageSource.addMessage("skip-message-key", ENGLISH, "SKIP PAGE");
        staticMessageSource.addMessage("not-skip-message-key", ENGLISH, "NOT SKIP PAGE");
        staticMessageSource.addMessage("page-to-skip-title", ENGLISH, pageToSkip);
        staticMessageSource.addMessage("last-page-title", ENGLISH, lastPageTitle);
    }

    @Test
    void shouldNotRenderPageAndNavigateToTheNextPageIfTheSkipConditionIsTrue() throws Exception {
        postExpectingNextPageTitle("firstPage", "someRadioInputName", "SKIP", thirdPageTitle);
    }

    @Test
    void shouldSupportSkippingMoreThanOnePageInARow() throws Exception {
        postExpectingNextPageTitle("firstPage", Map.of(
                "someRadioInputName", List.of("SKIP"),
                "radioInputToSkipThirdPage", List.of("SKIP")
        ), fourthPageTitle);
    }

    @Test
    void shouldRenderPageIfTheSkipConditionIsFalse() throws Exception {
        postExpectingNextPageTitle("firstPage", "someRadioInputName", "NOT_SKIP", secondPageTitle);
    }


    @Test
    void skipConditionBasedOnPageGroupData() throws Exception {
        postExpectingRedirect("sixthPage", "foo", "goToSeventhPage", "seventhPage");
        postExpectingRedirect("seventhPage", "foo", "SKIP", "eighthPage");
        postExpectingRedirect("eighthPage", "fourthPage");
    }
}
