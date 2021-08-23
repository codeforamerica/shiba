package org.codeforamerica.shiba.framework;

import static java.util.Locale.ENGLISH;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.codeforamerica.shiba.testutilities.AbstractFrameworkTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {"pagesConfig=pages-config/test-conditional-inputs.yaml"})
public class ConditionalInputsTest extends AbstractFrameworkTest {

  @Override
  @BeforeEach
  protected void setUp() throws Exception {
    super.setUp();
    staticMessageSource.addMessage("option1", ENGLISH, "option 1");
    staticMessageSource.addMessage("option2", ENGLISH, "option 2");
    staticMessageSource.addMessage("option3", ENGLISH, "option 3");
  }

  @Test
  void shouldOnlyRenderInputsBasedOnCondition() throws Exception {
    var page = postAndFollowRedirect("firstPage", "options", List.of("option1", "option2"));
    assertThat(page.getInputByName("option1Text")).isNotNull();
    assertThat(page.getInputByName("option2Text")).isNotNull();
    assertThat(page.getInputByName("option3Text")).isNull();
  }
}
