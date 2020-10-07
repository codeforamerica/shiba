package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.YamlPropertySourceFactory;
import org.codeforamerica.shiba.pages.config.ApplicationConfiguration;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.pages.YesNoAnswer.YES;

@Import(UserDecisionNavigationPageTest.TestController.class)
public class UserDecisionNavigationPageTest extends AbstractExistingStartTimePageTest {

    private final String optionZeroPageTitle = "page zero title";
    private final String optionOnePageTitle = "page one title";
    private final String yesAnswerTitle = "yes answer title";

    @TestConfiguration
    @PropertySource(value = "classpath:pages-config/test-user-decision-navigation.yaml", factory = YamlPropertySourceFactory.class)
    static class TestPageConfiguration {
        @Bean
        @ConfigurationProperties(prefix = "shiba-configuration-user-decision-navigation")
        public ApplicationConfiguration applicationConfiguration() {
            return new ApplicationConfiguration();
        }
    }

    @Controller
    static class TestController {
        private final ApplicationData applicationData;

        public TestController(ApplicationData applicationData) {
            this.applicationData = applicationData;
        }

        @GetMapping("/pathExposingFlow")
        ModelAndView endpointExposingFlow() {
            return new ModelAndView("viewExposingFlow", "flow", applicationData.getFlow());
        }
    }

    @BeforeEach
    void setUp() throws IOException {
        super.setUp();
        staticMessageSource.addMessage("option-zero-page-title", Locale.US, optionZeroPageTitle);
        staticMessageSource.addMessage("option-one-page-title", Locale.US, optionOnePageTitle);
        staticMessageSource.addMessage("yes-answer-title", Locale.US, yesAnswerTitle);
    }

    @Test
    void shouldNavigateToOptionZeroPageWhenUserSelectOptionOne() {
        driver.navigate().to(baseUrl + "/pages/userDecisionNavigationPage");
        driver.findElement(By.partialLinkText("option 1")).click();

        assertThat(driver.getTitle()).isEqualTo(optionOnePageTitle);
    }

    @Test
    void shouldSetTheFlow_fromTheSelectedOption() {
        driver.navigate().to(baseUrl + "/pathExposingFlow");
        assertThat(driver.findElement(By.id("flow")).getText()).isEmpty();

        driver.navigate().to(baseUrl + "/pages/userDecisionNavigationPage");
        driver.findElement(By.partialLinkText("option 1")).click();

        driver.navigate().to(baseUrl + "/pathExposingFlow");
        assertThat(driver.findElement(By.id("flow")).getText()).isEqualTo("FULL");
    }

    @Test
    void shouldNotUnsetTheFlow_ifTheSelectedOptionDoesNotHaveOne() {
        driver.navigate().to(baseUrl + "/pathExposingFlow");
        assertThat(driver.findElement(By.id("flow")).getText()).isEmpty();

        driver.navigate().to(baseUrl + "/pages/userDecisionNavigationPage");
        driver.findElement(By.partialLinkText("option 1")).click();
        driver.navigate().back();
        driver.findElement(By.partialLinkText("option 0")).click();

        driver.navigate().to(baseUrl + "/pathExposingFlow");
        assertThat(driver.findElement(By.id("flow")).getText()).isEqualTo("FULL");

    }

    @Test
    void shouldNavigateToNextPageBasedOnCondition() {
        navigateTo("formPageBranchingNavigationPage");
        testPage.choose(YES);

        assertThat(driver.getTitle()).isEqualTo(yesAnswerTitle);
    }
}
