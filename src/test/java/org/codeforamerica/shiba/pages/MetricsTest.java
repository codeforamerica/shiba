package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.AbstractShibaMockMvcTest;
import org.codeforamerica.shiba.framework.FormPage;
import org.codeforamerica.shiba.pages.config.FeatureFlag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@Tag("ccap")
//@Sql(statements = "TRUNCATE TABLE applications;")
public class MetricsTest extends AbstractShibaMockMvcTest {

    @BeforeEach
    protected void setUp() throws Exception {
        super.setUp();
        when(featureFlagConfiguration.get("apply-without-address")).thenReturn(FeatureFlag.OFF);
        mockMvc.perform(get("/pages/languagePreferences").session(session)); // start timer
        postExpectingSuccess("languagePreferences", Map.of(
                "writtenLanguage", List.of("ENGLISH"),
                "spokenLanguage", List.of("ENGLISH"))
        );
    }

    @Test
    @WithMockUser(username="admin",roles={"USER","ADMIN"})
    void userCanCompleteTheNonExpeditedFlowAndCanDownloadPdfsAndShibaShouldCaptureMetricsAfterApplicationIsCompleted() throws Exception {

        when(clock.instant()).thenReturn(
                LocalDateTime.of(2020, 1, 1, 10, 10).atOffset(ZoneOffset.UTC).toInstant(),
                LocalDateTime.of(2020, 1, 1, 10, 15, 30).atOffset(ZoneOffset.UTC).toInstant()
        );
        FormPage successPage = nonExpeditedFlowToSuccessPage(false, true);

        assert(successPage.findLinksByText("Combined Application").size() == 1);
        assert(successPage.findLinksByText("Child Care Application").size() == 1);

        postExpectingSuccess("success", "sentiment", "HAPPY");

        var metricsPage = new FormPage(getPageWithAuth("metrics"));

        assert(metricsPage.findElementTextById("totals")).contains("Totals");

        /*

        // Submitting Feedback

        driver.navigate().to(baseUrlWithAuth + "/metrics");
        MetricsPage metricsPage = new MetricsPage(driver);
        assertThat(metricsPage.getCardValue("Applications Submitted")).isEqualTo("1");
        assertThat(metricsPage.getCardValue("Median All Time")).contains("05m 30s");
        assertThat(metricsPage.getCardValue("Median Week to Date")).contains("05m 30s");
        assertThat(metricsPage.getCardValue("Average Week to Date")).contains("05m 30s");
        // When adding new counties, this TD will be equal to the first county in the list
        assertThat(driver.findElements(By.tagName("td")).get(0).getText()).isEqualTo("Anoka");
        assertThat(driver.findElements(By.tagName("td")).get(1).getText()).isEqualTo("0");
        assertThat(driver.findElements(By.tagName("td")).get(2).getText()).isEqualTo("0");
        assertThat(metricsPage.getCardValue("Happy")).contains("100%");

         */
    }
}

