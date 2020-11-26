package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.AbstractExistingStartTimePageTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {
        "spring.main.allow-bean-definition-overriding=true",
        "pagesConfig=pages-config/test-page-model.yaml"
})
public class PageModelPageTest extends AbstractExistingStartTimePageTest {

    String title;
    String subtleLink;
    String subtleLinkTitle;

    @Override
    @BeforeEach
    protected void setUp() throws java.io.IOException {
        super.setUp();

        title = "first page title";
        subtleLink = "subtle link text";
        subtleLinkTitle = "subtle link title";
        staticMessageSource.addMessage("first-page-title", Locale.ENGLISH, title);
        staticMessageSource.addMessage("subtle-link-text", Locale.ENGLISH, subtleLink);
        staticMessageSource.addMessage("subtle-link-page-title", Locale.ENGLISH, subtleLinkTitle);
    }

    @Test
    void shouldRenderThePageMatchingTheWorkflowPageName() {
        navigateTo("firstPage");

        assertThat(testPage.getTitle()).isEqualTo(title);
    }

    @Test
    void shouldRenderTheConfiguredPageModel() {
        navigateTo("lastPage");

        assertThat(testPage.getTitle()).isEqualTo(title);
    }

    @Test
    void shouldRenderTheConfiguredSubtleLink() {
        navigateTo("subtleLinkPage");
        assertThat(testPage.driver.findElementById("subtle-link").getText()).isEqualTo(subtleLink);
    }

    @Test
    void shouldNavigateToSubtleLinkTargetPage() {
        navigateTo("subtleLinkPage");
        testPage.clickLink(subtleLink);
        assertThat(testPage.getTitle()).isEqualTo(title);
    }

    @Test
    void shouldSubmitDataForTheConfiguredPageModel() {
        navigateTo("lastPage");

        String expectedValue = "some value";
        testPage.enter("someInput", expectedValue);
        testPage.clickContinue();

        assertThat(testPage.getInputValue("someInput")).isEqualTo(expectedValue);
    }
}
