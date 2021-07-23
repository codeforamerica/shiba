package org.codeforamerica.shiba.framework;

import org.codeforamerica.shiba.testutilities.AbstractStaticMessageSourceFrameworkTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {"pagesConfig=pages-config/test-start-timer.yaml"})
public class StartTimerPageTest extends AbstractStaticMessageSourceFrameworkTest {
    @Test
    void doesNotStartTheTimerIfPageIsNotConfiguredToStartTimer() throws Exception {
        getPageAndExpectRedirect("pageThatDoesNotStartTimer", "testStaticLandingPage");
        assertThat(applicationData.getStartTime()).isNull();
    }

    @Test
    void startsTheTimerIfPageIsConfiguredToStartTimer() throws Exception {
        getPage("pageThatDoesStartTimer");
        assertThat(applicationData.getStartTime()).isNotNull();
    }

    @Test
    void doesNotResetStartTime() throws Exception {
        getPage("pageThatDoesStartTimer");
        assertThat(applicationData.getStartTime()).isNotNull();
        var initialStartTime = applicationData.getStartTime();

        getPage("pageThatDoesStartTimer");
        assertThat(applicationData.getStartTime()).isEqualTo(initialStartTime);
    }
}
