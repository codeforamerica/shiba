package org.codeforamerica.shiba.framework;

import static java.util.Locale.ENGLISH;
import static org.mockito.Mockito.when;

import org.codeforamerica.shiba.pages.config.FeatureFlag;
import org.codeforamerica.shiba.testutilities.AbstractFrameworkTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {"pagesConfig=pages-config/test-feature-flag.yaml"})
public class FeatureFlagTest extends AbstractFrameworkTest {

  @Override
  @BeforeEach
  public void setUp() throws Exception {
    super.setUp();
    staticMessageSource.addMessage("first-page-title", ENGLISH, "firstPage");
    staticMessageSource.addMessage("second-page-title", ENGLISH, "secondPage");
    staticMessageSource.addMessage("first-feature-page-title", ENGLISH, "firstFeature");
    staticMessageSource.addMessage("second-feature-page-title", ENGLISH, "secondFeature");
    staticMessageSource.addMessage("third-page-title", ENGLISH, "thirdPage");
    staticMessageSource.addMessage("conditional-feature-page-title", ENGLISH, "conditionalFeature");
    when(featureFlagConfiguration.get("first-feature")).thenReturn(FeatureFlag.ON);
    when(featureFlagConfiguration.get("second-feature")).thenReturn(FeatureFlag.OFF);

  }

  @Test
  void shouldGoToSpecificPageIfFeatureFlagIsEnabled() throws Exception {
    postExpectingNextPageTitle("firstPage", "foo", "bar", "firstFeature");
  }

  @Test
  void shouldGoToOtherNextPageIfFeatureFlagIsDisabled() throws Exception {
    postExpectingNextPageTitle("firstFeaturePage", "foo", "bar", "secondPage");
  }

  @Test
  void shouldGoToNextPageIfFeatureFlagIsUnset() throws Exception {
    postExpectingNextPageTitle("secondPage", "foo", "bar", "firstPage");
  }

  @Test
  void shouldUseConfigOrderToDeterminePagePrecedenceAndIgnoreFlag() throws Exception {
    postExpectingNextPageTitle("secondFeaturePage", "foo", "bar", "secondPage");
  }

  @Test
  void shouldGoToSpecificPageIfFeatureFlagIsEnabledAndConditionIsSatisfied() throws Exception {
    postExpectingNextPageTitle("thirdPage", "foo", "yes", "conditionalFeature");

  }

  @Test
  void shouldNotGoToSpecificPageIfFeatureFlagIsEnabledAndConditionIsNotSatisfied()
      throws Exception {
    postExpectingNextPageTitle("thirdPage", "foo", "no", "firstPage");

  }

}
