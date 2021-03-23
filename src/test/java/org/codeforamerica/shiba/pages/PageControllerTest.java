package org.codeforamerica.shiba.pages;

import com.amazonaws.services.s3.transfer.TransferManager;
import org.codeforamerica.shiba.UploadDocumentConfiguration;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.ApplicationFactory;
import org.codeforamerica.shiba.application.ApplicationRepository;
import org.codeforamerica.shiba.application.FlowType;
import org.codeforamerica.shiba.application.parsers.ApplicationDataParser;
import org.codeforamerica.shiba.application.parsers.DocumentListParser;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.pages.config.ApplicationConfiguration;
import org.codeforamerica.shiba.pages.config.FeatureFlagConfiguration;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.pages.enrichment.ApplicationEnrichment;
import org.codeforamerica.shiba.pages.events.ApplicationSubmittedEvent;
import org.codeforamerica.shiba.pages.events.PageEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.*;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {
        "spring.main.allow-bean-definition-overriding=true",
        "pagesConfig=pages-config/test-pages-controller.yaml"
})
class PageControllerTest {

    private final StaticMessageSource messageSource = new StaticMessageSource();
    private final ApplicationEnrichment applicationEnrichment = mock(ApplicationEnrichment.class);

    ApplicationData applicationData = new ApplicationData();
    MockMvc mockMvc;
    Clock clock = mock(Clock.class);
    ApplicationRepository applicationRepository = mock(ApplicationRepository.class);
    ApplicationFactory applicationFactory = mock(ApplicationFactory.class);
    PageEventPublisher pageEventPublisher = mock(PageEventPublisher.class);
    ApplicationDataParser<List<Document>> documentListParser = mock(DocumentListParser.class);
    FeatureFlagConfiguration featureFlags = mock(FeatureFlagConfiguration.class);
    UploadDocumentConfiguration uploadDocumentConfiguration = mock(UploadDocumentConfiguration.class);

    @Autowired
    private ApplicationConfiguration applicationConfiguration;

    @Autowired
    private TransferManager transferManager;

    @BeforeEach
    void setUp() {
        PageController pageController = new PageController(
                applicationConfiguration,
                applicationData,
                clock,
                applicationRepository,
                applicationFactory,
                messageSource,
                pageEventPublisher,
                applicationEnrichment,
                documentListParser,
                featureFlags,
                transferManager,
                uploadDocumentConfiguration);

        mockMvc = MockMvcBuilders.standaloneSetup(pageController)
                .build();
        when(clock.instant()).thenReturn(Instant.now());
        when(applicationFactory.newApplication(any())).thenReturn(Application.builder()
                .id("defaultId")
                .completedAt(ZonedDateTime.now())
                .applicationData(null)
                .county(null)
                .timeToComplete(null)
                .build());
        messageSource.addMessage("success.feedback-success", Locale.ENGLISH, "default success message");
        messageSource.addMessage("success.feedback-failure", Locale.ENGLISH, "default failure message");
    }

    @Test
    void shouldWriteTheInputDataMapForSubmitPage() throws Exception {
        applicationData.setStartTimeOnce(Instant.now());
        when(clock.instant()).thenReturn(LocalDateTime.of(2020, 1, 1, 10, 10).atOffset(ZoneOffset.UTC).toInstant());

        mockMvc.perform(post("/submit")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .param("foo[]", "some value"))
                .andExpect(redirectedUrl("/pages/firstPage/navigation"));

        PageData firstPage = applicationData.getPagesData().getPage("firstPage");
        assertThat(firstPage.get("foo").getValue()).contains("some value");
    }

    @Test
    void shouldPublishApplicationSubmittedEvent() throws Exception {
        applicationData.setStartTimeOnce(Instant.now());

        String applicationId = "someId";
        Application application = Application.builder()
                .id(applicationId)
                .completedAt(ZonedDateTime.now())
                .applicationData(applicationData)
                .county(null)
                .timeToComplete(null)
                .flow(FlowType.FULL)
                .build();
        when(applicationFactory.newApplication(eq(applicationData))).thenReturn(application);

        String sessionId = "someSessionId";
        MockHttpSession session = new MockHttpSession(null, sessionId);
        mockMvc.perform(post("/submit")
                .session(session)
                .param("foo[]", "some value")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE));

        InOrder inOrder = inOrder(applicationRepository, pageEventPublisher);
        inOrder.verify(applicationRepository).save(application);
        inOrder.verify(pageEventPublisher).publish(new ApplicationSubmittedEvent(sessionId, applicationId, FlowType.FULL, Locale.ENGLISH));
    }

    @Test
    void shouldSaveApplication() throws Exception {
        applicationData.setStartTimeOnce(Instant.now());

        ZonedDateTime completedAt = ZonedDateTime.now();
        String applicationId = "someId";
        Application application = Application.builder()
                .id(applicationId)
                .completedAt(completedAt)
                .applicationData(applicationData)
                .county(null)
                .timeToComplete(null)
                .build();
        when(applicationRepository.getNextId()).thenReturn(applicationId);
        when(applicationFactory.newApplication(applicationData)).thenReturn(application);

        mockMvc.perform(post("/submit")
                .param("foo[]", "some value")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE));

        verify(applicationRepository).save(application);
        assertThat(applicationData.getId()).isEqualTo(applicationId);
    }

    @Test
    void shouldUpdateApplicationWithAllFeedbackIndicatorsAndIncludeSuccessMessage() throws Exception {
        String successMessage = "yay thanks for the feedback!";
        Locale locale = Locale.JAPANESE;
        messageSource.addMessage("success.feedback-success", locale, successMessage);

        String applicationId = "14356236";
        applicationData.setId(applicationId);
        Application application = Application.builder()
                .id("appIdFromDb")
                .build();
        when(applicationRepository.find(applicationId)).thenReturn(application);

        String feedback = "this was awesome!";
        mockMvc.perform(post("/submit-feedback")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .locale(locale)
                .param("sentiment", "HAPPY")
                .param("feedback", feedback))
                .andExpect(redirectedUrl("/pages/terminalPage"))
                .andExpect(flash().attribute("feedbackSuccess", equalTo(successMessage)));

        verify(applicationRepository).save(Application.builder()
                .id(application.getId())
                .sentiment(Sentiment.HAPPY)
                .feedback(feedback)
                .build());
    }

    @Test
    void shouldUpdateApplicationWithFeedback() throws Exception {
        String successMessage = "yay thanks for the feedback!";
        Locale locale = Locale.GERMAN;
        messageSource.addMessage("success.feedback-success", locale, successMessage);

        String applicationId = "14356236";
        applicationData.setId(applicationId);
        Application application = Application.builder()
                .id("appIdFromDb")
                .build();
        when(applicationRepository.find(applicationId)).thenReturn(application);

        String feedback = "this was awesome!";
        mockMvc.perform(post("/submit-feedback")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .locale(locale)
                .param("feedback", feedback))
                .andExpect(redirectedUrl("/pages/terminalPage"))
                .andExpect(flash().attribute("feedbackSuccess", equalTo(successMessage)));

        verify(applicationRepository).save(Application.builder()
                .id(application.getId())
                .feedback(feedback)
                .build());
    }

    @Test
    void shouldUpdateApplicationWithSentiment() throws Exception {
        String successMessage = "yay thanks for the feedback!";
        String ratingSuccessMessage = "yay thanks for the rating!";
        Locale locale = Locale.GERMAN;
        messageSource.addMessage("success.feedback-success", locale, successMessage);
        messageSource.addMessage("success.feedback-rating-success", locale, ratingSuccessMessage);

        String applicationId = "14356236";
        applicationData.setId(applicationId);
        Application application = Application.builder()
                .id("appIdFromDb")
                .build();
        when(applicationRepository.find(applicationId)).thenReturn(application);

        mockMvc.perform(post("/submit-feedback")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .locale(locale)
                .param("sentiment", "HAPPY"))
                .andExpect(redirectedUrl("/pages/terminalPage"))
                .andExpect(flash().attribute("feedbackSuccess", equalTo(ratingSuccessMessage)));

        verify(applicationRepository).save(Application.builder()
                .id(application.getId())
                .sentiment(Sentiment.HAPPY)
                .build());
    }

    @Test
    void shouldFailToSubmitFeedbackIfjIdIsNotSet() throws Exception {
        mockMvc.perform(post("/submit-feedback")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("sentiment", "HAPPY")
                .param("feedback", "this was awesome!"))
                .andExpect(redirectedUrl("/pages/terminalPage"));

        verifyNoInteractions(applicationRepository);
    }

    @Test
    void shouldFailToSubmitFeedbackAndIncludeFailureMessageIfNeitherSentimentNorFeedbackIsSupplied() throws Exception {
        String failureMessage = "bummer, that didn't work";
        Locale locale = Locale.ITALIAN;
        messageSource.addMessage("success.feedback-failure", locale, failureMessage);

        String applicationId = "14356236";
        applicationData.setId(applicationId);

        mockMvc.perform(post("/submit-feedback")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("feedback", "")
                .locale(locale))
                .andExpect(redirectedUrl("/pages/terminalPage"))
                .andExpect(flash().attribute("feedbackFailure", equalTo(failureMessage)));

        verifyNoInteractions(applicationRepository);
    }
}
