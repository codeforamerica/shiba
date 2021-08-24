package org.codeforamerica.shiba.framework;

import static java.util.Locale.ENGLISH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.application.FlowType.UNDETERMINED;

import org.codeforamerica.shiba.application.FlowType;
import org.codeforamerica.shiba.testutilities.AbstractFrameworkTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {"pagesConfig=pages-config/test-user-decision-navigation.yaml"})
public class UserDecisionNavigationTest extends AbstractFrameworkTest {

  private final String yesAnswerTitle = "yes answer title";

  @BeforeEach
  protected void setUp() throws Exception {
    super.setUp();
    staticMessageSource.addMessage("option-zero-page-title", ENGLISH, "page zero title");
    staticMessageSource.addMessage("option-one-page-title", ENGLISH, "page one title");
    staticMessageSource.addMessage("yes-answer-title", ENGLISH, yesAnswerTitle);
  }

  @Test
  void shouldNavigateToOptionZeroPageWhenUserSelectOptionOne() throws Exception {
    getNavigationPageWithQueryParamAndExpectRedirect("userDecisionNavigationPage", "option", "1",
        "optionOnePage");
  }

  @Test
  void shouldSetTheFlow_fromTheSelectedOption() throws Exception {
    assertThat(applicationData.getFlow()).isEqualTo(UNDETERMINED);

    var userDecisionNavigationPage = getFormPage("userDecisionNavigationPage");
    assertThat(userDecisionNavigationPage.getLinksContainingText("option 1")).isNotNull();
    getNavigationPageWithQueryParamAndExpectRedirect("userDecisionNavigationPage", "option", "1",
        "optionOnePage");

    assertThat(applicationData.getFlow()).isEqualTo(FlowType.FULL);
  }

  @Test
  void shouldNotUnsetTheFlow_ifTheSelectedOptionDoesNotHaveOne() throws Exception {
    assertThat(applicationData.getFlow()).isEqualTo(UNDETERMINED);

    var userDecisionNavigationPage = getFormPage("userDecisionNavigationPage");
    assertThat(userDecisionNavigationPage.getLinksContainingText("option 1")).isNotNull();
    getNavigationPageWithQueryParamAndExpectRedirect("userDecisionNavigationPage", "option", "1",
        "optionOnePage");
    // "Go back" and select a different option
    getNavigationPageWithQueryParamAndExpectRedirect("userDecisionNavigationPage", "option", "0",
        "optionZeroPage");

    assertThat(applicationData.getFlow()).isEqualTo(FlowType.FULL);
  }

  @Test
  void shouldNavigateToNextPageBasedOnCondition() throws Exception {
    postExpectingNextPageTitle("formPageBranchingNavigationPage", "yesNoQuestion", "true",
        yesAnswerTitle);
  }
}
