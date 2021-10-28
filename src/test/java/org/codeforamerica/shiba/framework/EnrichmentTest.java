package org.codeforamerica.shiba.framework;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.codeforamerica.shiba.pages.data.InputData;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.codeforamerica.shiba.pages.enrichment.Enrichment;
import org.codeforamerica.shiba.testutilities.AbstractFrameworkTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest(properties = {"pagesConfig=pages-config/test-enrichment.yaml"})
@ContextConfiguration(classes = EnrichmentTest.EnrichmentTestConfiguration.class)
public class EnrichmentTest extends AbstractFrameworkTest {

  @Test
  void enrichesThePageDataWithTheEnrichmentResults() throws Exception {
    var page = postAndFollowRedirect("testEnrichmentPage", "someTextInput", "someText");
    assertThat(page.getElementTextById("originalInput")).isEqualTo("someText");
    assertThat(page.getElementTextById("enrichmentInput"))
        .isEqualTo("someText-someEnrichmentValue");
  }

  @Test
  void doesNotEnrichThePageDataIfPageDataIsInvalid() throws Exception {
    postExpectingFailure("testEnrichmentPage", "someTextInput", "");
    assertThat(getFormPage("testEnrichmentPage").getElementById("enrichmentInput")).isNull();
  }

  @TestConfiguration
  static class EnrichmentTestConfiguration {

    @Bean
    public Enrichment testEnrichment() {
      return new TestEnrichment();
    }
  }

  static class TestEnrichment implements Enrichment {

    @Override
    public PageData process(PagesData pagesData) {
      String pageInputValue = pagesData
          .get("testEnrichmentPage")
          .get("someTextInput")
          .getValue().get(0);
      return new PageData(Map.of(
          "someEnrichmentInput", new InputData(List.of(pageInputValue + "-someEnrichmentValue"))
      ));
    }
  }
}
