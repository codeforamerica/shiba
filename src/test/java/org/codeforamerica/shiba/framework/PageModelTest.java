package org.codeforamerica.shiba.framework;

import org.codeforamerica.shiba.testutilities.AbstractFrameworkTest;
import org.codeforamerica.shiba.testutilities.FormPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static java.util.Locale.ENGLISH;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {"pagesConfig=pages-config/test-page-model.yaml"})
public class PageModelTest extends AbstractFrameworkTest {
    private String title;
    private String subtleLink;

    @Override
    @BeforeEach
    protected void setUp() throws Exception {
        super.setUp();

        title = "first page title";
        subtleLink = "subtle link text";
        staticMessageSource.addMessage("first-page-title", ENGLISH, title);
        staticMessageSource.addMessage("subtle-link-text", ENGLISH, subtleLink);
        staticMessageSource.addMessage("subtle-link-page-title", ENGLISH, "subtle link title");
    }

    @Test
    void shouldRenderThePageMatchingTheWorkflowPageName() throws Exception {
        assertThat(getFormPage("firstPage").getTitle()).isEqualTo(title);
    }

    @Test
    void shouldRenderTheConfiguredPageModel() throws Exception {
        assertThat(getFormPage("lastPage").getTitle()).isEqualTo(title);
    }

    @Test
    void shouldRenderTheConfiguredSubtleLink() throws Exception {
        var page = getFormPage("subtleLinkPage");
        assertThat(page.findElementTextById("subtle-link")).isEqualTo(subtleLink);
    }

    @Test
    void shouldNavigateToSubtleLinkTargetPage() throws Exception {
        var page = new FormPage(getPage("subtleLinkPage"));
        page.assertLinkWithTextHasCorrectUrl(subtleLink, "/pages/firstPage");
    }

    @Test
    void shouldSubmitDataForTheConfiguredPageModel() throws Exception {
        var firstPage = postAndFollowRedirect("lastPage", "someInput", "some value");
        // firstPage and lastPage have the same model so the input should already be filled
        assertThat(firstPage.getInputValue("someInput")).isEqualTo("some value");
    }
}
