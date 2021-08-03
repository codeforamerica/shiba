package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.testutilities.AbstractShibaMockMvcTest;
import org.codeforamerica.shiba.testutilities.FormPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@Tag("ccap")
@Sql(statements = "TRUNCATE TABLE applications;")
public class MetricsTest extends AbstractShibaMockMvcTest {

    @BeforeEach
    protected void setUp() throws Exception {
        super.setUp();
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

        assertThat(successPage.getLinksContainingText("Combined Application")).hasSize(1);
        assertThat(successPage.getLinksContainingText("Child Care Application")).hasSize(1);
        mockMvc.perform(post("/submit-feedback").session(session).with(csrf()).param("sentiment", "HAPPY"));

        FormPage metricsPage = new FormPage(getPageWithAuth("metrics"));

        assertThat(metricsPage.getElementTextById("totals")).contains("Totals");
        assertThat(metricsPage.getCardValue("Happy")).contains("100%");
        assertThat(metricsPage.getCardValue("Applications Submitted")).isEqualTo("1");
        assertThat(metricsPage.getCardValue("Median All Time")).contains("05m 30s");
        assertThat(metricsPage.getCardValue("Median Week to Date")).contains("05m 30s");
        assertThat(metricsPage.getCardValue("Average Week to Date")).contains("05m 30s");
        // When adding new counties, this TD will be equal to the first county in the list
        assertThat(metricsPage.getElementsByTag("td").get(0).ownText()).contains("Aitkin");
        assertThat(metricsPage.getElementsByTag("td").get(1).ownText()).contains("0");
        assertThat(metricsPage.getElementsByTag("td").get(2).ownText()).contains("0");
        assertThat(metricsPage.getCardValue("Happy")).contains("100%");
    }
}

