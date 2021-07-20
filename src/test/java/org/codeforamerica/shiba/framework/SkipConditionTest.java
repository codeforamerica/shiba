package org.codeforamerica.shiba.framework;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Locale;

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
    void shouldNotRenderPageAndNavigateToTheNextPageIfTheSkipConditionIsTrue() throws Exception {
        postExpectingNextPageTitle("firstPage", "someRadioInputName", "SKIP", thirdPageTitle);
    }

}
