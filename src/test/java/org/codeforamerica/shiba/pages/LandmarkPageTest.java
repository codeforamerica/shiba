package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.AbstractStaticMessageSourcePageTest;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.events.ApplicationSubmittedListener;
import org.codeforamerica.shiba.pages.events.PageEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@Import(LandmarkPageTest.TestController.class)
@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {"pagesConfig=pages-config/test-landmark-pages.yaml"})
public class LandmarkPageTest extends AbstractStaticMessageSourcePageTest {
    String firstPageTitle = "first page title";
    String fourthPageTitle = "fourth page title";

    @MockBean
    private PageEventPublisher pageEventPublisher;

    @MockBean
    private ApplicationSubmittedListener applicationSubmittedListener;

    private static ApplicationData applicationData;

    @Controller
    static class TestController {
        private final ApplicationData applicationData;

        public TestController(ApplicationData applicationData) {
            this.applicationData = applicationData;
        }

        @GetMapping("/testPath")
        String testEndpoint() {
            ApplicationData applicationDataClone = new ApplicationData();
            applicationDataClone.setPagesData(this.applicationData.getPagesData());
            applicationDataClone.setSubworkflows(this.applicationData.getSubworkflows());
            applicationDataClone.setIncompleteIterations(this.applicationData.getIncompleteIterations());
            applicationDataClone.setId(this.applicationData.getId());
            applicationDataClone.setStartTime(this.applicationData.getStartTime());
            LandmarkPageTest.applicationData = applicationDataClone;
            return "testTerminalPage";
        }
    }

    @Override
    @BeforeEach
    protected void setUp() throws IOException {
        super.setUp();
        staticMessageSource.addMessage("first-page-title", Locale.ENGLISH, firstPageTitle);
        staticMessageSource.addMessage("third-page-title", Locale.ENGLISH, "third page title");
        staticMessageSource.addMessage("fourth-page-title", Locale.ENGLISH, fourthPageTitle);
    }

    @Test
    void shouldRenderTheFirstLandingPageFromTheRoot() {
        driver.navigate().to(baseUrl);

        assertThat(testPage.getTitle()).isEqualTo(firstPageTitle);
    }

    @Test
    void shouldRedirectToFirstLandingPageWhenNavigateToAMidFlowPageDirectly() {
        navigateTo("fourthPage");

        assertThat(testPage.getTitle()).isEqualTo(firstPageTitle);
    }

    @Test
    void shouldNotRedirectToFirstLandingPageWhenNavigateToAMidFlowPageAfterStartTimerPage() {
        navigateTo("thirdPage");
        testPage.clickContinue();

        assertThat(testPage.getTitle()).isEqualTo(fourthPageTitle);
    }

    @Test
    void shouldRedirectToTerminalPageWhenUserBacksFromTerminalPage() {
        navigateTo("thirdPage");
        testPage.clickContinue();

        driver.navigate().back();

        assertThat(testPage.getTitle()).isEqualTo(fourthPageTitle);
    }

    @Test
    void shouldRedirectToTerminalPageWhenUserNavigatesToANonLandingPage() {
        navigateTo("thirdPage");
        testPage.clickContinue();
        navigateTo("thirdPage");

        assertThat(testPage.getTitle()).isEqualTo(fourthPageTitle);
    }

    @Test
    void shouldNotRedirectWhenUserNavigateToALandingPage() {
        navigateTo("thirdPage");
        testPage.clickContinue();
        navigateTo("firstPage");

        assertThat(testPage.getTitle()).isEqualTo(firstPageTitle);
    }

    @Test
    void shouldClearTheSessionWhenUserNavigatesToALandingPage() {
        navigateTo("firstPage");

        testPage.enter("foo", "someInput");
        testPage.clickContinue();

        testPage.clickContinue();

        navigateTo("firstPage");
        driver.navigate().to(baseUrl + "/testPath");
        assertThat(LandmarkPageTest.applicationData).isEqualTo(new ApplicationData());
    }
}
