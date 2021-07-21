package org.codeforamerica.shiba.framework;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

import static java.util.Locale.ENGLISH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {"pagesConfig=pages-config/test-landmark-pages.yaml"})
public class LandmarkPageMockMvcTest extends AbstractStaticMessageSourceFrameworkTest {
    private final String firstPageTitle = "first page title";

    @Override
    @BeforeEach
    protected void setUp() throws Exception {
        super.setUp();
        staticMessageSource.addMessage("first-page-title", ENGLISH, firstPageTitle);
        staticMessageSource.addMessage("second-page-title", ENGLISH, "second page title");
        staticMessageSource.addMessage("third-page-title", ENGLISH, "third page title");
        staticMessageSource.addMessage("fourth-page-title", ENGLISH, "fourth page title");
    }

    @Test
    void shouldRenderTheFirstLandingPageFromTheRoot() throws Exception {
        mockMvc.perform(get("/").session(session)).andExpect(forwardedUrl("/pages/testStaticLandingPage"));
        assertThat(getFormPage("testStaticLandingPage").getTitle()).isEqualTo(firstPageTitle);
    }

    @Test
    void shouldRedirectToFirstLandingPageWhenNavigateToAMidFlowPageDirectly() throws Exception {
        getPageAndExpectRedirect("fourthPage", "testStaticLandingPage");
    }

    @Test
    void shouldRedirectToTerminalPageWhenUserBacksFromTerminalPage() throws Exception {
        getPage("thirdPage").andExpect(status().isOk()); // start timer page
        submitThirdPage();
        // "Go back"
        getPageAndExpectRedirect("thirdPage", "fourthPage");
    }

    @Test
    void shouldNotRedirectWhenUserNavigatesToALandingPage() throws Exception {
        getPage("thirdPage").andExpect(status().isOk()); // start timer page
        submitThirdPage();
        var page = getFormPage("testStaticLandingPage");
        assertThat(page.getTitle()).isEqualTo(firstPageTitle);
    }

    // We have a special helper for this because the third page is the submitPage
    private void submitThirdPage() throws Exception {
        postToUrlExpectingSuccess("/submit", "/pages/thirdPage/navigation", Map.of());
        assertNavigationRedirectsToCorrectNextPage("thirdPage", "fourthPage");
    }
}
