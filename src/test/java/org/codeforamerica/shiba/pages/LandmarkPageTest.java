package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.AbstractStaticMessageSourcePageTest;
import org.codeforamerica.shiba.YamlPropertySourceFactory;
import org.codeforamerica.shiba.pages.config.ApplicationConfiguration;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.events.ApplicationSubmittedListener;
import org.codeforamerica.shiba.pages.events.PageEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

@Import(LandmarkPageTest.TestController.class)
public class LandmarkPageTest extends AbstractStaticMessageSourcePageTest {
    @TestConfiguration
    @PropertySource(value = "classpath:pages-config/test-landmark-pages.yaml", factory = YamlPropertySourceFactory.class)
    static class TestPageConfiguration {
        @Bean
        @ConfigurationProperties(prefix = "shiba-configuration-landmarks")
        public ApplicationConfiguration applicationConfiguration() {
            return new ApplicationConfiguration();
        }
    }

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
        staticMessageSource.addMessage("first-page-title", Locale.US, firstPageTitle);
        staticMessageSource.addMessage("third-page-title", Locale.US, "third page title");
        staticMessageSource.addMessage("fourth-page-title", Locale.US, fourthPageTitle);
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
