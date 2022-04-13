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

    staticMessageSource.addMessage("containsDesiredOption",ENGLISH, "Contains Desired Option");
    staticMessageSource.addMessage("containsExtraOption1", ENGLISH, "Contain Extra Option 1");
    staticMessageSource.addMessage("containsExtraOption2",ENGLISH, "Contains Extra Option 2");
  }

  @Test
  void shouldOnlyRenderInputsBasedOnCondition() throws Exception {
    var page = postAndFollowRedirect("firstPage", "options", List.of("option1", "option2"));
    assertThat(page.getInputByName("option1Text")).isNotNull();
    assertThat(page.getInputByName("option2Text")).isNotNull();
    assertThat(page.getInputByName("option3Text")).isNull();
  }

  /**
   * DOES_NOT_EQUAL is the value matcher tested here!   DOES_NOT_EQUAL should take in a value as
   * an input and if the value is not found or there are more values than the value that you are evaluating then
   * this function should return false
   */
  @Test
  void shouldNotDisplayInputIfUserSelectedOnlySpecifiedValue__forValueMatcherDoesNotEqual() throws Exception {
    //if user selects only the desired option then the test shouldNotDisplayContentIfMultipleValuesSelected should not be displayed
    var page = postAndFollowRedirect("thirdPage", "containsOptions", List.of("containsDesiredOption"));
    assertThat(page.getInputByName("shouldNotDisplayContentIfMultipleValuesSelected")).isNull();
    assertThat(page.getInputByName("shouldDisplayRegardless")).isNotNull();

    //Properly displays question when more than one option is selected
    var page2 = postAndFollowRedirect("thirdPage", "containsOptions", List.of("containsDesiredOption", "containsExtraOption1"));
    assertThat(page2.getInputByName("shouldNotDisplayContentIfMultipleValuesSelected")).isNotNull();
    assertThat(page2.getInputByName("shouldDisplayRegardless")).isNotNull();
  }
}
