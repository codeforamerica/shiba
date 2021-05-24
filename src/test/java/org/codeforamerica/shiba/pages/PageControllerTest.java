package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.ApplicationStatusUpdater;
import org.codeforamerica.shiba.NonSessionScopedApplicationData;
import org.codeforamerica.shiba.application.*;
import org.codeforamerica.shiba.pages.config.FeatureFlag;
import org.codeforamerica.shiba.pages.config.FeatureFlagConfiguration;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.pages.events.ApplicationSubmittedEvent;
import org.codeforamerica.shiba.pages.events.PageEventPublisher;
import org.codeforamerica.shiba.pages.events.UploadedDocumentsSubmittedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.*;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = MOCK, properties = {"pagesConfig=pages-config/test-pages-controller.yaml"})
@ContextConfiguration(classes = {NonSessionScopedApplicationData.class})
class PageControllerTest {
    @MockBean
    private MessageSource messageSource;
    private MockMvc mockMvc;

    @MockBean
    private Clock clock;
    @MockBean
    private ApplicationRepository applicationRepository;
    @MockBean
    private ApplicationFactory applicationFactory;
    @MockBean
    private PageEventPublisher pageEventPublisher;
    @MockBean
    private FeatureFlagConfiguration featureFlags;

    @Autowired
    private PageController pageController;

    @Autowired
    private ApplicationData applicationData;

    @MockBean
    private ApplicationStatusUpdater applicationStatusUpdater;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(pageController).build();
        when(clock.instant()).thenReturn(Instant.now());
        when(applicationFactory.newApplication(any())).thenReturn(Application.builder()
                .id("defaultId")
                .completedAt(ZonedDateTime.now())
                .applicationData(null)
                .county(null)
                .timeToComplete(null)
                .build());
        when(messageSource.getMessage(eq("success.feedback-success"), any(), eq(Locale.ENGLISH))).thenReturn("default success message");
        when(messageSource.getMessage(eq("success.feedback-failure"), any(), eq(Locale.ENGLISH))).thenReturn("default failure message");
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
    void shouldPublishUploadedDocumentsSubmittedEvent() throws Exception {
        String applicationId = "someId";
        applicationData.setId(applicationId);

        MockMultipartFile image = new MockMultipartFile("image", "someImage.jpg", MediaType.IMAGE_JPEG_VALUE, "test".getBytes());
        applicationData.addUploadedDoc(image, "someS3FilePath", "someDataUrl", "image/jpeg");

        Application application = Application.builder()
                .id(applicationId)
                .completedAt(ZonedDateTime.now())
                .applicationData(applicationData)
                .county(null)
                .timeToComplete(null)
                .flow(FlowType.FULL)
                .build();
        when(applicationRepository.find(eq(applicationId))).thenReturn(application);
        when(featureFlags.get("submit-via-api")).thenReturn(FeatureFlag.ON);

        String sessionId = "someSessionId";
        MockHttpSession session = new MockHttpSession(null, sessionId);
        mockMvc.perform(post("/submit-documents")
                .session(session)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE));

        InOrder inOrder = inOrder(applicationRepository, pageEventPublisher);
        inOrder.verify(applicationRepository).save(application);
        inOrder.verify(pageEventPublisher).publish(new UploadedDocumentsSubmittedEvent(sessionId, applicationId, Locale.ENGLISH));
    }

    @Test
    void shouldSaveApplicationOnEveryFormSubmission() throws Exception {
        applicationData.setStartTimeOnce(Instant.now());

        String applicationId = "someId";
        applicationData.setId(applicationId);
        Application application = Application.builder()
                .id(applicationId)
                .applicationData(applicationData)
                .build();
        when(applicationRepository.getNextId()).thenReturn(applicationId);
        when(applicationFactory.newApplication(applicationData)).thenReturn(application);

        mockMvc.perform(post("/pages/firstPage")
                .param("foo[]", "some value")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE));

        verify(applicationRepository).save(application);
        assertThat(applicationData.getId()).isEqualTo(applicationId);
    }

    @Test
    void shouldSaveApplicationOnSignaturePage() throws Exception {
        applicationData.setStartTimeOnce(Instant.now());

        ZonedDateTime completedAt = ZonedDateTime.now();
        String applicationId = "someId";
        applicationData.setId(applicationId);
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
        when(messageSource.getMessage(eq("success.feedback-success"), any(), eq(locale))).thenReturn(successMessage);

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
        when(messageSource.getMessage(eq("success.feedback-success"), any(), eq(locale))).thenReturn(successMessage);

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
        when(messageSource.getMessage(eq("success.feedback-success"), any(), eq(locale))).thenReturn(successMessage);
        when(messageSource.getMessage(eq("success.feedback-rating-success"), any(), eq(locale))).thenReturn(ratingSuccessMessage);

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
    void shouldFailToSubmitFeedbackIfIdIsNotSet() throws Exception {
        mockMvc.perform(post("/submit-feedback")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("sentiment", "HAPPY")
                .param("feedback", "this was awesome!"))
                .andExpect(redirectedUrl("/pages/terminalPage"));

        verify(applicationRepository, never()).save(any());
    }

    @Test
    void shouldFailToSubmitFeedbackAndIncludeFailureMessageIfNeitherSentimentNorFeedbackIsSupplied() throws Exception {
        String failureMessage = "bummer, that didn't work";
        Locale locale = Locale.ITALIAN;
        when(messageSource.getMessage(eq("success.feedback-failure"), any(), eq(locale))).thenReturn(failureMessage);

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

    @Test
    void shouldUpdateUploadDocumentsStatusWhenUploadDocumentsPageIsReached() throws Exception {
        applicationData.setStartTimeOnce(Instant.now());

        String applicationId = "someId";
        applicationData.setId(applicationId);
        Application application = Application.builder()
                .id(applicationId)
                .applicationData(applicationData)
                .build();
        when(applicationRepository.getNextId()).thenReturn(applicationId);
        when(applicationFactory.newApplication(applicationData)).thenReturn(application);

        mockMvc.perform(get("/pages/uploadDocuments"));

        verify(applicationStatusUpdater).updateUploadedDocumentsStatus(application.getId(), Status.IN_PROGRESS);
    }

    @Test
    void shouldUpdateMultipleApplicationStatusesWhenChoosingPrograms() throws Exception {
        applicationData.setStartTimeOnce(Instant.now());
        String applicationId = "someId";
        applicationData.setId(applicationId);
        Application application = Application.builder()
                .id(applicationId)
                .applicationData(applicationData)
                .build();
        when(applicationRepository.getNextId()).thenReturn(applicationId);
        when(applicationFactory.newApplication(applicationData)).thenReturn(application);

        mockMvc.perform(post("/pages/choosePrograms")
                .param("programs[]", "CCAP", "SNAP")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE));

        verify(applicationStatusUpdater).updateCcapApplicationStatus(application.getId(), Status.IN_PROGRESS);
        verify(applicationStatusUpdater).updateCafApplicationStatus(application.getId(), Status.IN_PROGRESS);
    }

    @Test
    void shouldUpdateCafApplicationStatusWhenChoosingPrograms() throws Exception {
        applicationData.setStartTimeOnce(Instant.now());
        String applicationId = "someId";
        applicationData.setId(applicationId);
        Application application = Application.builder()
                .id(applicationId)
                .applicationData(applicationData)
                .build();
        when(applicationRepository.getNextId()).thenReturn(applicationId);
        when(applicationFactory.newApplication(applicationData)).thenReturn(application);

        mockMvc.perform(post("/pages/choosePrograms")
                .param("programs[]", "SNAP")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE));

        verify(applicationStatusUpdater, never()).updateCcapApplicationStatus(application.getId(), Status.IN_PROGRESS);
        verify(applicationStatusUpdater).updateCafApplicationStatus(application.getId(), Status.IN_PROGRESS);
    }

    @Test
    void shouldUpdateCcapApplicationStatusWhenChoosingPrograms() throws Exception {
        applicationData.setStartTimeOnce(Instant.now());
        String applicationId = "someId";
        applicationData.setId(applicationId);
        Application application = Application.builder()
                .id(applicationId)
                .applicationData(applicationData)
                .build();
        when(applicationRepository.getNextId()).thenReturn(applicationId);
        when(applicationFactory.newApplication(applicationData)).thenReturn(application);

        mockMvc.perform(post("/pages/choosePrograms")
                .param("programs[]", "CCAP")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE));

        verify(applicationStatusUpdater).updateCcapApplicationStatus(application.getId(), Status.IN_PROGRESS);
        verify(applicationStatusUpdater, never()).updateCafApplicationStatus(application.getId(), Status.IN_PROGRESS);
    }

    @Test
    void shouldUpdateLastPageWhenVisitingANewPage() throws Exception {
        applicationData.setStartTimeOnce(Instant.now());
        String applicationId = "someId";
        applicationData.setId(applicationId);
        Application application = Application.builder()
                .id(applicationId)
                .applicationData(applicationData)
                .build();
        when(applicationRepository.getNextId()).thenReturn(applicationId);
        when(applicationFactory.newApplication(applicationData)).thenReturn(application);

        mockMvc.perform(get("/pages/choosePrograms"));

        assertThat(applicationData.getLastPage()).isEqualTo("choosePrograms");
    }
}
