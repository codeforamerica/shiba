package org.codeforamerica.shiba.framework;

import static java.util.Locale.ENGLISH;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.codeforamerica.shiba.testutilities.AbstractFrameworkTest;
import org.codeforamerica.shiba.testutilities.FormPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {"pagesConfig=pages-config/test-conditional-rendering.yaml"})
public class SkipConditionTest extends AbstractFrameworkTest {

  private final String fourthPageTitle = "fourthPageTitle";
  private final String thirdPageTitle = "thirdPageTitle";
  private final String secondPageTitle = "secondPageTitle";
  private final String firstPageTitle = "firstPageTitle";
  private final String eighthPageTitle = "eighthPageTitle";
  private final String pageToSkipTitle = "pageToSkip";
  private final String lastPageTitle = "lastPageTitle";

  @BeforeEach
  protected void setUp() throws Exception {
    super.setUp();
    staticMessageSource.addMessage("starting-page-title", ENGLISH, "starting page");
    staticMessageSource.addMessage("first-page-title", ENGLISH, firstPageTitle);
    staticMessageSource.addMessage("second-page-title", ENGLISH, secondPageTitle);
    staticMessageSource.addMessage("third-page-title", ENGLISH, thirdPageTitle);
    staticMessageSource.addMessage("fourth-page-title", ENGLISH, fourthPageTitle);
    staticMessageSource.addMessage("eighth-page-title", ENGLISH, eighthPageTitle);
    staticMessageSource.addMessage("ninth-page-title", ENGLISH, "ninthPageTitle");
    staticMessageSource.addMessage("skip-message-key", ENGLISH, "SKIP PAGE");
    staticMessageSource.addMessage("not-skip-message-key", ENGLISH, "NOT SKIP PAGE");
    staticMessageSource.addMessage("page-to-skip-title", ENGLISH, pageToSkipTitle);
    staticMessageSource.addMessage("last-page-title", ENGLISH, lastPageTitle);
  }

  @Test
  void shouldNotRenderPageAndNavigateToTheNextPageIfTheSkipConditionIsTrue() throws Exception {
    postExpectingNextPageTitle("firstPage", "someRadioInputName", "SKIP", thirdPageTitle);
  }

  @Test
  void shouldSupportSkippingMoreThanOnePageInARow() throws Exception {
    postExpectingNextPageTitle("firstPage", Map.of(
        "someRadioInputName", List.of("SKIP"),
        "radioInputToSkipThirdPage", List.of("SKIP")
    ), fourthPageTitle);
  }

  @Test
  void shouldRenderPageIfTheSkipConditionIsFalse() throws Exception {
    postExpectingNextPageTitle("firstPage", "someRadioInputName", "NOT_SKIP", secondPageTitle);
  }


  @Test
  void skipConditionBasedOnPageGroupData() throws Exception {
    postExpectingRedirect("sixthPage", "foo", "goToSeventhPage", "seventhPage");
    postExpectingRedirect("seventhPage", "foo", "SKIP", "eighthPage");
    postExpectingRedirect("eighthPage", "fourthPage");
  }

  @Test
  void shouldRemoveDataForSkippedPage() throws Exception {
    postExpectingRedirect("firstPage", "someRadioInputName", "NOT_SKIP", "secondPage");
    postExpectingRedirect("secondPage", "foo", "something", "thirdPage");

    // Go back to first page and enter value that will cause secondPage to be skipped
    postExpectingRedirect("firstPage", "someRadioInputName", "SKIP", "thirdPage");

    // Go back to first page and enter value that will cause secondPage NOT to be skipped.
    postExpectingRedirect("firstPage", "someRadioInputName", "NOT_SKIP", "secondPage");
    var secondPage = new FormPage(getPage("secondPage"));
    assertThat(secondPage.getTitle()).isEqualTo(secondPageTitle);

    // Assert that secondPage's previous input data has been cleared out
    assertThat(secondPage.getInputValue("foo")).isEmpty();
  }

  @Test
  void shouldNavigateToTheFirstNextPageWhoseConditionIsTrue() throws Exception {
    postExpectingNextPageTitle("fourthPage", "foo", "goToFirstPage", firstPageTitle);
  }

  @Test
  void shouldGoToFirstNextPageWhoseConditionIsTrue_forSubworkflow() throws Exception {
    postExpectingNextPageTitle("sixthPage", "foo", "goToEighthPage", eighthPageTitle);
  }

  @Test
  void shouldSupportConditionalRenderingForMultipleConditions() throws Exception {
    postExpectingNextPageTitle("startingPage", Map.of(
        "randomInput", List.of("someTextInput"),
        "anotherInput", List.of("AnotherTextInput")
    ), lastPageTitle);
  }

  @Test
  void shouldNotSkipIfMultipleConditionsAreNotMet() throws Exception {
    postExpectingNextPageTitle("startingPage", Map.of(
        "randomInput", List.of("someTextInput"),
        "anotherInput", List.of("notCorrectInput")
    ), pageToSkipTitle);
  }

  @Test
  void shouldSupportConditionalRenderingForMultipleConditionsWithOrOperator() throws Exception {
    postExpectingNextPageTitle("secondStartingPage", Map.of(
        "randomInput", List.of("someTextInput"),
        "anotherInput", List.of("notCorrectInput")
    ), lastPageTitle);
  }

  @Test
  void shouldNotSkipIfMultipleConditionsAreNotMetWithOrOperator() throws Exception {
    postExpectingNextPageTitle("secondStartingPage", Map.of(
        "randomInput", List.of("notCorrectInput"),
        "anotherInput", List.of("alsoNotCorrectInput")
    ), pageToSkipTitle);
  }
}
