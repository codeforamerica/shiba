package org.codeforamerica.shiba;

import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.time.Instant;

@Import(AbstractExistingStartTimePageTest.TestController.class)
public class AbstractExistingStartTimePageTest extends AbstractStaticMessageSourcePageTest {
    @Override
    @BeforeEach
    protected void setUp() throws IOException {
        super.setUp();
        driver.navigate().to(baseUrl + "/setStartTimeForTest");
    }

    @Controller
    static class TestController {
        private final ApplicationData applicationData;

        public TestController(ApplicationData applicationData) {
            this.applicationData = applicationData;
        }

        @GetMapping("/setStartTimeForTest")
        String setStartTimeForTest() {
            applicationData.setStartTime(Instant.now());
            return "startTimeIsSet";
        }
    }
}
