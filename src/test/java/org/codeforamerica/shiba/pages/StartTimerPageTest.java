package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.YamlPropertySourceFactory;
import org.codeforamerica.shiba.metrics.Metrics;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.format.datetime.standard.InstantFormatter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Import(StartTimerPageTest.TestController.class)
public class StartTimerPageTest extends AbstractStaticMessageSourcePageTest {

    @TestConfiguration
    @PropertySource(value = "classpath:test-start-timer.yaml", factory = YamlPropertySourceFactory.class)
    static class TestPageConfiguration {
        @Bean
        @ConfigurationProperties(prefix = "shiba-configuration-start-timer")
        public PagesConfiguration pagesConfiguration() {
            return new PagesConfiguration();
        }
    }

    @Controller
    static class TestController {
        private final Metrics metrics;

        public TestController(Metrics metrics) {
            this.metrics = metrics;
        }

        @GetMapping("/pathExposingStartTime")
        ModelAndView endpointExposingStartTime() {
            String startTime = Optional.ofNullable(metrics.getStartTime())
                    .map(instant -> new InstantFormatter().print(instant, Locale.US))
                    .orElse("");
            return new ModelAndView("viewExposingStartTime", Map.of("startTime", startTime));
        }
    }

    @Test
    void doesNotStartTheTimerIfPageIsNotConfiguredToStartTimer() {
        driver.navigate().to(baseUrl + "/pages/pageThatDoesNotStartTimer");

        driver.navigate().to(baseUrl + "/pathExposingStartTime");

        assertThat(driver.findElement(By.id("start-time")).getText()).isEqualTo("");
    }

    @Test
    void startsTheTimerIfPageIsConfiguredToStartTimer() {
        driver.navigate().to(baseUrl + "/pages/pageThatDoesStartTimer");

        driver.navigate().to(baseUrl + "/pathExposingStartTime");

        assertThat(driver.findElement(By.id("start-time")).getText()).isNotBlank();
    }
}
