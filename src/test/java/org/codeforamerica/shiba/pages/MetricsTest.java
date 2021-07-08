package org.codeforamerica.shiba.pages;

import lombok.extern.slf4j.Slf4j;
import org.codeforamerica.shiba.AbstractShibaMockMvcTest;
import org.codeforamerica.shiba.framework.FormPage;
import org.codeforamerica.shiba.pages.config.FeatureFlag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@Tag("ccap")
@Sql(statements = "TRUNCATE TABLE applications;")
@Slf4j
public class MetricsTest extends AbstractShibaMockMvcTest {

    @BeforeEach
    protected void setUp() throws Exception {
        super.setUp();
        when(featureFlagConfiguration.get("apply-without-address")).thenReturn(FeatureFlag.OFF);
        when(clock.instant()).thenReturn(
                LocalDateTime.of(2020, 1, 1, 10, 10).atOffset(ZoneOffset.UTC).toInstant(),
                LocalDateTime.of(2020, 1, 1, 10, 15, 30).atOffset(ZoneOffset.UTC).toInstant()
        );
        mockMvc.perform(get("/pages/languagePreferences").session(session)); // start timer
        postExpectingSuccess("languagePreferences", Map.of(
                "writtenLanguage", List.of("ENGLISH"),
                "spokenLanguage", List.of("ENGLISH"))
        );
    }

    @Test
    @WithMockUser(username = "admin", roles = {"USER", "ADMIN"})
    void userCanCompleteTheNonExpeditedFlowAndCanDownloadPdfsAndShibaShouldCaptureMetricsAfterApplicationIsCompleted() throws Exception {
        FormPage successPage = nonExpeditedFlowToSuccessPage(false, true);

        assertThat(successPage.findLinksByText("Combined Application")).hasSize(1);
        assertThat(successPage.findLinksByText("Child Care Application")).hasSize(1);
        mockMvc.perform(post("/submit-feedback").session(session).with(csrf()).param("sentiment", "HAPPY"));

        log.info("test123");
        FormPage metricsPage = new FormPage(getPageWithAuth("metrics"));

        assertThat(metricsPage.findElementTextById("totals")).contains("Totals");
        assertThat(metricsPage.getCardValue("Happy")).contains("100%");
        assertThat(metricsPage.getCardValue("Applications Submitted")).isEqualTo("1");
        assertThat(metricsPage.getCardValue("Median All Time")).contains("05m 30s");
        assertThat(metricsPage.getCardValue("Median Week to Date")).contains("05m 30s");
        assertThat(metricsPage.getCardValue("Average Week to Date")).contains("05m 30s");
        // When adding new counties, this TD will be equal to the first county in the list
        assertThat(metricsPage.findElementsByTag("td").get(0).ownText()).contains("Anoka");
        assertThat(metricsPage.findElementsByTag("td").get(1).ownText()).contains("0");
        assertThat(metricsPage.findElementsByTag("td").get(2).ownText()).contains("0");
        assertThat(metricsPage.getCardValue("Happy")).contains("100%");
    }
}

