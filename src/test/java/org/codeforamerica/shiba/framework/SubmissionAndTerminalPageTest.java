package org.codeforamerica.shiba.framework;

import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.County.Hennepin;
import static org.codeforamerica.shiba.pages.Sentiment.HAPPY;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.ApplicationRepository;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.events.ApplicationSubmittedListener;
import org.codeforamerica.shiba.testutilities.AbstractStaticMessageSourceFrameworkTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest(properties = {"pagesConfig=pages-config/test-submit-page.yaml"})
public class SubmissionAndTerminalPageTest extends AbstractStaticMessageSourceFrameworkTest {

  @MockBean
  private ApplicationSubmittedListener applicationSubmittedListener;
  @MockBean
  private ApplicationRepository applicationRepository;

  @Test
  void shouldProvideApplicationDataToTerminalPageWhenApplicationIsSigned() throws Exception {
    var applicationId = "someId";
    var county = Hennepin;
    var applicationData = mock(ApplicationData.class);
    when(applicationData.isCAFApplication()).thenReturn(true);
    when(applicationData.isCCAPApplication()).thenReturn(true);
    var sentiment = HAPPY;
    var feedbackText = "someFeedback";
    Application application = Application.builder()
        .id(applicationId)
        .completedAt(ZonedDateTime.of(LocalDateTime.of(2020, 1, 1, 11, 10), UTC))
        .applicationData(applicationData)
        .county(county)
        .timeToComplete(Duration.ofSeconds(124))
        .sentiment(sentiment)
        .feedback(feedbackText)
        .build();
    when(applicationRepository.find(any())).thenReturn(application);

    assertThat(getFormPage("firstPage").getInputByName("foo")).isNotNull();
    postToUrlExpectingSuccess("/submit",
        "/pages/firstPage/navigation",
        Map.of("foo", List.of("some value")));

    var testTerminalPage = getNextPageAsFormPage("firstPage");
    assertThat(testTerminalPage.getElementTextById("submission-time"))
        .isEqualTo("2020-01-01T05:10-06:00[America/Chicago]");
    assertThat(testTerminalPage.getElementTextById("application-id")).isEqualTo(applicationId);
    assertThat(testTerminalPage.getElementTextById("county")).isEqualTo(county.name());
    assertThat(testTerminalPage.getElementTextById("sentiment")).isEqualTo(sentiment.name());
    assertThat(testTerminalPage.getElementTextById("feedback-text")).isEqualTo(feedbackText);
    assertThat(testTerminalPage.getElementTextById("CAF")).contains("CAF");
    assertThat(testTerminalPage.getElementTextById("CCAP")).contains("CCAP");
  }
}
