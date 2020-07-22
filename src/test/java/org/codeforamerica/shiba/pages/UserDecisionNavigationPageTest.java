package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.YamlPropertySourceFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;

import java.io.IOException;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.pages.YesNoAnswer.YES;

public class UserDecisionNavigationPageTest extends AbstractStaticMessageSourcePageTest {

    private final String optionZeroPageTitle = "page zero title";
    private final String optionOnePageTitle = "page one title";
    private final String yesAnswerTitle = "yes answer title";

    @TestConfiguration
    @PropertySource(value = "classpath:pages-config/test-user-decision-navigation.yaml", factory = YamlPropertySourceFactory.class)
    static class TestPageConfiguration extends MetricsTestConfigurationWithExistingStartTime {
        @Bean
        @ConfigurationProperties(prefix = "shiba-configuration-user-decision-navigation")
        public PagesConfiguration pagesConfiguration() {
            return new PagesConfiguration();
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
    void shouldNavigateToNextPageBasedOnCondition() {
        navigateTo("formPageBranchingNavigationPage");
        testPage.choose(YES);

        assertThat(driver.getTitle()).isEqualTo(yesAnswerTitle);
    }
}
