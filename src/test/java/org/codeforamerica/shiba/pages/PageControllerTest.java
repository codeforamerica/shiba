package org.codeforamerica.shiba.pages;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.County.Anoka;
import static org.codeforamerica.shiba.application.FlowType.LATER_DOCS;
import static org.codeforamerica.shiba.application.Status.IN_PROGRESS;
import static org.codeforamerica.shiba.output.Document.CAF;
import static org.codeforamerica.shiba.output.Document.CCAP;
import static org.codeforamerica.shiba.output.Document.UPLOADED_DOC;
import static org.codeforamerica.shiba.testutilities.TestUtils.resetApplicationData;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.*;
import java.util.List;
import java.util.Locale;
import org.codeforamerica.shiba.DocumentRepositoryTestConfig;
import org.codeforamerica.shiba.TribalNationRoutingDestination;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.ApplicationFactory;
import org.codeforamerica.shiba.application.ApplicationRepository;
import org.codeforamerica.shiba.application.DocumentStatusRepository;
import org.codeforamerica.shiba.application.FlowType;
import org.codeforamerica.shiba.documents.DocumentRepository;
import org.codeforamerica.shiba.mnit.CountyRoutingDestination;
import org.codeforamerica.shiba.mnit.RoutingDestination;
import org.codeforamerica.shiba.pages.config.FeatureFlag;
import org.codeforamerica.shiba.pages.config.FeatureFlagConfiguration;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.pages.events.ApplicationSubmittedEvent;
import org.codeforamerica.shiba.pages.events.PageEventPublisher;
import org.codeforamerica.shiba.pages.events.UploadedDocumentsSubmittedEvent;
import org.codeforamerica.shiba.testutilities.NonSessionScopedApplicationData;
import org.codeforamerica.shiba.testutilities.TestApplicationDataBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ActiveProfiles("test")
@SpringBootTest(
    webEnvironment = MOCK,
    properties = {"pagesConfig=pages-config/test-pages-controller.yaml"})
@ContextConfiguration(classes = {NonSessionScopedApplicationData.class,
    DocumentRepositoryTestConfig.class})
class PageControllerTest {

  private MockMvc mockMvc;

  @MockBean
  private MessageSource messageSource;
  @MockBean
  private Clock clock;
  @MockBean
  private ApplicationRepository applicationRepository;
  @MockBean
  private DocumentStatusRepository documentStatusRepository;
  @MockBean
  private ApplicationFactory applicationFactory;
  @MockBean
  private PageEventPublisher pageEventPublisher;
  @MockBean
  private FeatureFlagConfiguration featureFlags;
  @MockBean
  private RoutingDecisionService routingDecisionService;
  @SpyBean
  private DocumentRepository documentRepository;

  @Autowired
  private PageController pageController;
  @Autowired
  private ApplicationData applicationData;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(pageController).build();
    when(clock.instant()).thenReturn(Instant.now());
    when(clock.getZone()).thenReturn(ZoneId.of("UTC"));
    when(applicationFactory.newApplication(any())).thenReturn(Application.builder()
        .id("defaultId")
        .completedAt(ZonedDateTime.now())
        .applicationData(applicationData)
        .county(null)
        .timeToComplete(null)
        .build());
    when(messageSource.getMessage(eq("success.feedback-success"), any(), eq(Locale.ENGLISH)))
        .thenReturn("default success message");
    when(messageSource.getMessage(eq("success.feedback-failure"), any(), eq(Locale.ENGLISH)))
        .thenReturn("default failure message");
  }

  @AfterEach
  void tearDown() {
    resetApplicationData(applicationData);
  }

  @Test
  void shouldWriteTheInputDataMapForSubmitPage() throws Exception {
    applicationData.setStartTimeOnce(Instant.now());
    when(clock.instant())
        .thenReturn(LocalDateTime.of(2020, 1, 1, 10, 10).atOffset(ZoneOffset.UTC).toInstant());

    MockHttpServletRequestBuilder request = post("/submit")
        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
        .param("foo[]", "some value");
    mockMvc.perform(request).andExpect(redirectedUrl("/pages/secondPage/navigation"));

    PageData secondPage = applicationData.getPagesData().getPage("secondPage");
    assertThat(secondPage.get("foo").getValue()).contains("some value");
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
    inOrder.verify(pageEventPublisher).publish(
        new ApplicationSubmittedEvent(sessionId, applicationId, FlowType.FULL, Locale.ENGLISH));
  }

  @Test
  void shouldPublishUploadedDocumentsSubmittedEvent() throws Exception {
    String applicationId = "someId";
    applicationData.setId(applicationId);

    MockMultipartFile image = new MockMultipartFile("image", "someImage.jpg",
        MediaType.IMAGE_JPEG_VALUE, "test".getBytes());
    applicationData.addUploadedDoc(image, "someS3FilePath", "someThumbnailFilepath", "image/jpeg");

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
    inOrder.verify(pageEventPublisher)
        .publish(new UploadedDocumentsSubmittedEvent(sessionId, applicationId, Locale.ENGLISH));
  }

  @Test
  void shouldSetCompletedAtAndTimeToCompleteForLaterDocsFlow() throws Exception {
    String applicationId = "someId";
    applicationData.setId(applicationId);
    applicationData.setStartTimeOnce(Instant.now());

    MockMultipartFile image = new MockMultipartFile("image", "someImage.jpg",
        MediaType.IMAGE_JPEG_VALUE, "test".getBytes());
    applicationData.addUploadedDoc(image, "someS3FilePath", "someDataUrl", "image/jpeg");

    Application application = Application.builder()
        .id(applicationId)
        .completedAt(ZonedDateTime.now())
        .applicationData(applicationData)
        .county(null)
        .timeToComplete(null)
        .flow(LATER_DOCS)
        .build();

    applicationData.setFlow(LATER_DOCS);

    when(applicationRepository.find(eq(applicationId))).thenReturn(application);
    when(featureFlags.get("submit-via-api")).thenReturn(FeatureFlag.ON);

    String sessionId = "someSessionId";

    assertThat(application.getTimeToComplete()).isEqualTo(null);

    MockHttpSession session = new MockHttpSession(null, sessionId);
    mockMvc.perform(post("/submit-documents")
        .session(session)
        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE));

    assertThat(application.getCompletedAt()).isNotNull();
    assertThat(application.getTimeToComplete()).isNotNull();
    InOrder inOrder = inOrder(applicationRepository, pageEventPublisher);
    inOrder.verify(applicationRepository).save(application);
    inOrder.verify(pageEventPublisher)
        .publish(new UploadedDocumentsSubmittedEvent(sessionId, applicationId, Locale.ENGLISH));
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

    mockMvc.perform(post("/pages/secondPage")
        .param("foo[]", "some other value")
        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE));

    verify(applicationRepository, times(2)).save(application);
    assertThat(applicationData.getId()).isEqualTo(applicationId);
  }

  @Test
  void shouldSaveApplicationStatusOnInProgressPages() throws Exception {
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

    mockMvc.perform(post("/pages/secondPage")
        .param("foo[]", "some other value")
        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE));

    verify(documentStatusRepository, times(1))
        .createOrUpdateAll(application, IN_PROGRESS);
    assertThat(applicationData.getId()).isEqualTo(applicationId);
  }

  @Test
  void shouldSaveApplicationOnSignaturePage() throws Exception {
    applicationData.setStartTimeOnce(Instant.now());

    String applicationId = "someId";
    applicationData.setId(applicationId);
    Application application = Application.builder()
        .id(applicationId)
        .applicationData(applicationData)
        .county(null)
        .build();

    when(applicationRepository.getNextId()).thenReturn(applicationId);
    when(applicationFactory.newApplication(applicationData)).thenReturn(application);

    assertThat(application.getTimeToComplete()).isEqualTo(null);

    mockMvc.perform(post("/submit")
        .param("foo[]", "some value")
        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE));

    verify(applicationRepository).save(application);
    assertThat(application.getTimeToComplete()).isNotNull();
    assertThat(application.getCompletedAt()).isNotNull();
    assertThat(applicationData.getId()).isEqualTo(applicationId);
  }

  @Test
  void shouldUpdateApplicationWithAllFeedbackIndicatorsAndIncludeSuccessMessage() throws Exception {
    String successMessage = "yay thanks for the feedback!";
    Locale locale = Locale.JAPANESE;
    when(messageSource.getMessage(eq("success.feedback-success"), any(), eq(locale)))
        .thenReturn(successMessage);

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
  void shouldUpdateRoutingDestinationsInTheDatabaseOnTerminalPage() throws Exception {
    applicationData.setStartTimeOnce(Instant.now());
    new TestApplicationDataBuilder(applicationData)
        .withApplicantPrograms(List.of("CASH", "CCAP"));

    String applicationId = "someId";
    applicationData.setId(applicationId);
    Application application = Application.builder()
        .id(applicationId)
        .completedAt(ZonedDateTime.now())
        .applicationData(applicationData)
        .county(Anoka)
        .build();

    when(routingDecisionService.getRoutingDestinations(applicationData, CAF)).thenReturn(List.of(
        new TribalNationRoutingDestination("Mille Lacs Band of Ojibwe")
    ));
    when(routingDecisionService.getRoutingDestinations(applicationData, CCAP)).thenReturn(List.of(
        new CountyRoutingDestination(Anoka, "dhsProviderId", "something@example.com",
            "8675309", null)
    ));
    when(applicationRepository.find(applicationId)).thenReturn(application);

    mockMvc.perform(get("/pages/terminalPage"));

    verify(applicationRepository).save(application);
  }

  @Test
  void shouldUpdateApplicationWithFeedback() throws Exception {
    String successMessage = "yay thanks for the feedback!";
    Locale locale = Locale.GERMAN;
    when(messageSource.getMessage(eq("success.feedback-success"), any(), eq(locale)))
        .thenReturn(successMessage);

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
    when(messageSource.getMessage(eq("success.feedback-success"), any(), eq(locale)))
        .thenReturn(successMessage);
    when(messageSource.getMessage(eq("success.feedback-rating-success"), any(), eq(locale)))
        .thenReturn(ratingSuccessMessage);

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
  void shouldFailToSubmitFeedbackAndIncludeFailureMessageIfNeitherSentimentNorFeedbackIsSupplied()
      throws Exception {
    String failureMessage = "bummer, that didn't work";
    Locale locale = Locale.ITALIAN;
    when(messageSource.getMessage(eq("success.feedback-failure"), any(), eq(locale)))
        .thenReturn(failureMessage);

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
    when(applicationRepository.find("someId")).thenReturn(application);
    when(applicationFactory.newApplication(applicationData)).thenReturn(application);
    List<RoutingDestination> routingDestinations =
        List.of(new TribalNationRoutingDestination("Mille Lacs Band of Ojibwe"));
    when(routingDecisionService.getRoutingDestinations(applicationData, UPLOADED_DOC)).thenReturn(
        routingDestinations);

    mockMvc.perform(
            MockMvcRequestBuilders.multipart("/document-upload")
                .file("file", "something".getBytes())
                .param("dataURL", "someDataUrl")
                .param("type", "jpg"))
        .andExpect(status().is(200));
    
    verify(documentStatusRepository).createOrUpdateAllForDocumentType(applicationData, IN_PROGRESS, UPLOADED_DOC);
  }

  @Test
  void shouldHandleMissingThumbnails() throws Exception {
    applicationData.setStartTimeOnce(Instant.now());
    var applicationId = "someId";
    applicationData.setId(applicationId);
    var image = new MockMultipartFile("image", "someImage.jpg", MediaType.IMAGE_JPEG_VALUE,
        "test".getBytes());
    applicationData.addUploadedDoc(image, "someS3FilePath", "someThumbnailFilepath", "image/jpeg");
    var application = Application.builder()
        .id(applicationId)
        .applicationData(applicationData)
        .build();
    when(applicationRepository.getNextId()).thenReturn(applicationId);
    when(applicationFactory.newApplication(applicationData)).thenReturn(application);

    when(documentRepository.get(any())).thenThrow(RuntimeException.class);

    mockMvc.perform(get("/pages/uploadDocuments")).andExpect(status().isOk());
  }

  /**
   * If an applicant completes and application and wants to submit a second application from the
   * same device then they should be able to navigate back to the beginning of the application and
   * continue through the application process.
   */
  @Test
  void shouldHandleMultipleApplicationsSequentially() throws Exception {
    // Start with a completed application
    String applicationId = "14356236";
    applicationData.setId(applicationId);
    applicationData.setStartTimeOnce(Instant.now());
    applicationData.setSubmitted(true);
    Application application = Application.builder()
        .id("14356236")
        .completedAt(ZonedDateTime.now())
        .build();
    when(applicationRepository.find(applicationId)).thenReturn(application);

    // Should bump back to landing page to invalidate the session and start a new application
    mockMvc.perform(get("/pages/firstPage")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .locale(Locale.GERMAN))
        .andExpect(redirectedUrl("/pages/landingPage"));
  }

  @Test
  void shouldRedirectToErrorPageWhenAnInvalidPageIsRequested() throws Exception {
    applicationData.setStartTimeOnce(Instant.now());
    String applicationId = "someId";
    applicationData.setId(applicationId);
    Application application = Application.builder()
        .id(applicationId)
        .applicationData(applicationData)
        .build();
    when(applicationRepository.getNextId()).thenReturn(applicationId);
    when(applicationFactory.newApplication(applicationData)).thenReturn(application);

    mockMvc.perform(get("/pages/doesNotExist")).andExpect(redirectedUrl("/error"));

    mockMvc.perform(get("/pages/doesNotExist/navigation")).andExpect(redirectedUrl("/error"));
  }
}
