package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.AbstractStaticMessageSourcePageTest;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.format.datetime.standard.InstantFormatter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@Import(StartTimerPageTest.TestController.class)
@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {
        "spring.main.allow-bean-definition-overriding=true",
        "pagesConfig=pages-config/test-start-timer.yaml"
})
public class StartTimerPageTest extends AbstractStaticMessageSourcePageTest {
    @Controller
    static class TestController {
        private final ApplicationData applicationData;

        public TestController(ApplicationData applicationData) {
            this.applicationData = applicationData;
        }

        @GetMapping("/pathExposingStartTime")
        ModelAndView endpointExposingStartTime() {
            String startTime = Optional.ofNullable(applicationData.getStartTime())
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

    @Test
    void doesNotResetStartTime() {
        driver.navigate().to(baseUrl + "/pages/pageThatDoesStartTimer");
        driver.navigate().to(baseUrl + "/pathExposingStartTime");
        String initialStartTime = driver.findElement(By.id("start-time")).getText();

        driver.navigate().to(baseUrl + "/pages/pageThatDoesStartTimer");
        driver.navigate().to(baseUrl + "/pathExposingStartTime");
        String startTime = driver.findElement(By.id("start-time")).getText();

        assertThat(startTime).isEqualTo(initialStartTime);
    }
}
