package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.AbstractExistingStartTimePageTest;
import org.codeforamerica.shiba.YamlPropertySourceFactory;
import org.codeforamerica.shiba.pages.config.ApplicationConfiguration;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.InputData;
import org.codeforamerica.shiba.pages.enrichment.ApplicationEnrichment;
import org.codeforamerica.shiba.pages.enrichment.Enrichment;
import org.codeforamerica.shiba.pages.enrichment.EnrichmentResult;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class EnrichmentPageTest extends AbstractExistingStartTimePageTest {

    @TestConfiguration
    @PropertySource(value = "classpath:pages-config/test-enrichment.yaml", factory = YamlPropertySourceFactory.class)
    static class TestPageConfiguration {
        @Bean
        @ConfigurationProperties(prefix = "shiba-configuration-enrichment")
        public ApplicationConfiguration applicationConfiguration() {
            return new ApplicationConfiguration();
        }

        @Bean
        public Enrichment testEnrichment() {
            return new TestEnrichment();
        }

    }

    static class TestEnrichment implements Enrichment {
        @Override
        public EnrichmentResult process(ApplicationData applicationData) {
            String pageInputValue = applicationData
                    .getPageData("testEnrichmentPage")
                    .get("someTextInput")
                    .getValue().get(0);
            return new EnrichmentResult(Map.of(
                    "someEnrichmentInput", new InputData(List.of(pageInputValue + "-someEnrichmentValue"))
            ));
        }
    }

    @Autowired
    ApplicationEnrichment applicationEnrichment;

    @Test
    void enrichesThePageDataWithTheEnrichmentResults() {
        navigateTo("testEnrichmentPage");
        testPage.enter("someTextInput", "someText");
        testPage.clickContinue();

        assertThat(driver.findElementById("originalInput").getText()).isEqualTo("someText");
        assertThat(driver.findElementById("enrichmentInput").getText()).isEqualTo("someText-someEnrichmentValue");
    }

    @Test
    void doesNotEnrichThePageDataIfPageDataIsInvalid() {
        navigateTo("testEnrichmentPage");
        testPage.clickContinue();

        assertThat(driver.findElements(By.id("enrichmentInput"))).isEmpty();
    }
}
