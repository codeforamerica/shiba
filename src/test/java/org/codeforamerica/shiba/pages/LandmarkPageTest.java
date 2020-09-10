package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.*;
import org.codeforamerica.shiba.metrics.Metrics;
import org.codeforamerica.shiba.pages.config.ApplicationConfiguration;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

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
    private ApplicationEventPublisher applicationEventPublisher;

    @MockBean
    private ApplicationFactory applicationFactory;

    @MockBean
    private ApplicationRepository applicationRepository;

    private static ApplicationData applicationData;
    private static Metrics metrics;
    private static ConfirmationData confirmationData;

    @Controller
    static class TestController {
        private final ApplicationData applicationData;
        private final Metrics metrics;
        private final ConfirmationData confirmationData;

        public TestController(ApplicationData applicationData,
                              Metrics metrics,
                              ConfirmationData confirmationData) {
            this.applicationData = applicationData;
            this.metrics = metrics;
            this.confirmationData = confirmationData;
        }

        @GetMapping("/testPath")
        String testEndpoint() {
            ApplicationData applicationDataClone = new ApplicationData();
            applicationDataClone.setPagesData(this.applicationData.getPagesData());
            applicationDataClone.setSubworkflows(this.applicationData.getSubworkflows());
            applicationDataClone.setIncompleteIterations(this.applicationData.getIncompleteIterations());
            LandmarkPageTest.applicationData = applicationDataClone;
            Metrics metricsClone = new Metrics();
            metricsClone.setStartTimeOnce(this.metrics.getStartTime());
            LandmarkPageTest.metrics = metricsClone;
            ConfirmationData confirmationDataClone = new ConfirmationData();
            confirmationDataClone.setId(this.confirmationData.getId());
            confirmationDataClone.setCompletedAt(this.confirmationData.getCompletedAt());
            confirmationDataClone.setCounty(this.confirmationData.getCounty());
            LandmarkPageTest.confirmationData = confirmationDataClone;
            return "testTerminalPage";
        }
    }

    @Override
    @BeforeEach
    void setUp() throws IOException {
        super.setUp();
        staticMessageSource.addMessage("first-page-title", Locale.US, firstPageTitle);
        staticMessageSource.addMessage("fourth-page-title", Locale.US, "fourth page title");
        staticMessageSource.addMessage("first-page-title", Locale.US, "first page title");
        staticMessageSource.addMessage("fourth-page-title", Locale.US, fourthPageTitle);
        when(applicationFactory.newApplication(any(), any())).thenReturn(new Application("foo", ZonedDateTime.now(), new ApplicationData(), County.OTHER, ""));
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
        testPage.clickPrimaryButton();

        assertThat(testPage.getTitle()).isEqualTo(fourthPageTitle);
    }

    @Test
    void shouldRedirectToTerminalPageWhenUserBacksFromTerminalPage() {
        navigateTo("thirdPage");
        testPage.clickPrimaryButton();

        driver.navigate().back();

        assertThat(testPage.getTitle()).isEqualTo(fourthPageTitle);
    }

    @Test
    void shouldRedirectToTerminalPageWhenUserNavigatesToANonLandingPage() {
        navigateTo("thirdPage");
        testPage.clickPrimaryButton();
        navigateTo("thirdPage");

        assertThat(testPage.getTitle()).isEqualTo(fourthPageTitle);
    }

    @Test
    void shouldNotRedirectWhenUserNavigateToALandingPage() {
        navigateTo("thirdPage");
        testPage.clickPrimaryButton();
        navigateTo("firstPage");

        assertThat(testPage.getTitle()).isEqualTo(firstPageTitle);
    }

    @Test
    void shouldClearTheSessionWhenUserNavigatesToALandingPage() {
        navigateTo("firstPage");

        testPage.enterInput("foo", "someInput");
        testPage.clickPrimaryButton();

        testPage.clickPrimaryButton();

        navigateTo("firstPage");
        driver.navigate().to(baseUrl + "/testPath");
        assertThat(LandmarkPageTest.applicationData).isEqualTo(new ApplicationData());
        assertThat(LandmarkPageTest.metrics).isEqualTo(new Metrics());
        assertThat(LandmarkPageTest.confirmationData).isEqualTo(new ConfirmationData());
    }
}
