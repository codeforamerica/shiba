package org.codeforamerica.shiba.pages;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.County.Anoka;
import static org.codeforamerica.shiba.TribalNation.MilleLacsBandOfOjibwe;
import static org.codeforamerica.shiba.application.FlowType.LATER_DOCS;
import static org.codeforamerica.shiba.output.Document.CAF;
import static org.codeforamerica.shiba.output.Document.CCAP;
import static org.codeforamerica.shiba.testutilities.TestUtils.resetApplicationData;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.ws.test.client.RequestMatchers.connectionTo;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.codeforamerica.shiba.DocumentRepositoryTestConfig;
import org.codeforamerica.shiba.TribalNationRoutingDestination;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.ApplicationFactory;
import org.codeforamerica.shiba.application.ApplicationRepository;
import org.codeforamerica.shiba.application.ApplicationStatusRepository;
import org.codeforamerica.shiba.application.FlowType;
import org.codeforamerica.shiba.documents.DocumentRepository;
import org.codeforamerica.shiba.mnit.CountyRoutingDestination;
import org.codeforamerica.shiba.output.caf.EligibilityListBuilder;
import org.codeforamerica.shiba.pages.config.FeatureFlag;
import org.codeforamerica.shiba.pages.config.FeatureFlagConfiguration;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.codeforamerica.shiba.pages.events.ApplicationSubmittedEvent;
import org.codeforamerica.shiba.pages.events.PageEventPublisher;
import org.codeforamerica.shiba.pages.events.UploadedDocumentsSubmittedEvent;
import org.codeforamerica.shiba.testutilities.ClientDevice;
import org.codeforamerica.shiba.testutilities.NonSessionScopedApplicationData;
import org.codeforamerica.shiba.testutilities.PagesDataBuilder;
import org.codeforamerica.shiba.testutilities.TestApplicationDataBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import mobi.openddr.classifier.model.Device;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.test.client.MockWebServiceServer;
import org.springframework.ws.test.client.ResponseCreators;
import org.springframework.xml.transform.StringSource;

@ActiveProfiles("test")
@SpringBootTest(
    webEnvironment = MOCK,
    properties = {"pagesConfig=pages-config/test-pages-controller.yaml"})
@ContextConfiguration(classes = {NonSessionScopedApplicationData.class,
    DocumentRepositoryTestConfig.class, ClientDevice.class})
class PageControllerTest {

  private MockMvc mockMvc;

  @MockBean
  private MessageSource messageSource;
  @MockBean
  private Clock clock;
  @MockBean
  private ApplicationRepository applicationRepository;
  @MockBean
  private ApplicationStatusRepository applicationStatusRepository;
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
  @SpyBean
  private Device device;
  @MockBean
  private EligibilityListBuilder listBuilder;
  @Autowired
  private PageController pageController;
  @Autowired
  private ApplicationData applicationData;
  @MockBean
  private MockWebServiceServer mockWebServiceServer;
  @Value("${mnit-clammit.url}")
  private String clammitUrl;

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
    mockWebServiceServer = MockWebServiceServer.createServer(new WebServiceTemplate());
        mockWebServiceServer.expect(connectionTo(clammitUrl))
        .andRespond(ResponseCreators.withPayload(new StringSource("200")));
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
        .requestAttr("currentDevice", device)
        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
        .param("foo[]", "some value");
    mockMvc.perform(request).andExpect(redirectedUrl("/pages/secondPage/navigation"));

    PageData secondPage = applicationData.getPagesData().getPage("secondPage");
    assertThat(secondPage.get("foo").getValue()).contains("some value");
    String devicePlatform = applicationData.getDevicePlatform();
    String deviceType = applicationData.getDeviceType();
    assertThat(devicePlatform).isEqualToIgnoringCase("Android");
    assertThat(deviceType).isEqualToIgnoringCase("mobile");
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
        .requestAttr("currentDevice", device)
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
        .requestAttr("currentDevice", device)
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
        new TribalNationRoutingDestination(MilleLacsBandOfOjibwe)
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
  void shouldReturnErrorForEmptyFileUpload() throws Exception {
    applicationData.setStartTimeOnce(Instant.now());

    String applicationId = "someId";
    applicationData.setId(applicationId);
    Application application = Application.builder()
        .id(applicationId)
        .applicationData(applicationData)
        .build();
    when(applicationRepository.find("someId")).thenReturn(application);
    when(applicationFactory.newApplication(applicationData)).thenReturn(application);

    mockMvc.perform(
            MockMvcRequestBuilders.multipart("/document-upload")
                .file("file", new byte[]{})
                .param("dataURL", "someDataUrl")
                .param("type", "jpeg"))
        .andExpect(status().isUnprocessableEntity());
  }

  @Test
	void shouldReturnErrorForFileWithVirus() throws Exception {
		mockWebServiceServer = MockWebServiceServer.createServer(new WebServiceTemplate());
		mockWebServiceServer.expect(connectionTo(clammitUrl))
				.andRespond(ResponseCreators.withPayload(new StringSource("418")));

		mockMvc.perform(MockMvcRequestBuilders.multipart("/document-upload").file("file", new byte[] {}).param("data",
				"X5O!P%@AP[4\\PZX54(P^)7CC)7}$EICAR-STANDARD-ANTIVIRUS-TEST-FILE!$H+H*"))
				.andExpect(status().is4xxClientError());
		mockWebServiceServer.reset();
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
    when(applicationRepository.find(applicationId)).thenReturn(application);

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

  @Test
  void shouldRedirectToUploadDocumentPageWhenGoingToSubmitConfirmationWithoutDocuments()
      throws Exception {
    applicationData.setStartTimeOnce(Instant.now());
    applicationData.setUploadedDocs(List.of());
    String applicationId = "someId";
    applicationData.setId(applicationId);
    Application application = Application.builder()
        .id(applicationId)
        .applicationData(applicationData)
        .build();
    when(applicationRepository.getNextId()).thenReturn(applicationId);
    when(applicationFactory.newApplication(applicationData)).thenReturn(application);
    when(applicationRepository.find(applicationId)).thenReturn(application);

    mockMvc.perform(get("/pages/documentSubmitConfirmation"))
        .andExpect(redirectedUrl("/pages/uploadDocuments"));
  }


  @Test
  void shouldUpdateTriageAnsWithCCAPChildNudgeAns() throws Exception {
    applicationData.setStartTimeOnce(Instant.now());
    String applicationId = "someId";
    PagesData pagesData =
        new PagesDataBuilder()
            .withPageData("addHouseholdMembers", Map.of("addHouseholdMembers", "true")).build();
    applicationData.setPagesData(pagesData);
    applicationData.setId(applicationId);
    Application application = Application.builder()
        .id(applicationId)
        .applicationData(applicationData)
        .build();
    when(applicationRepository.getNextId()).thenReturn(applicationId);
    when(applicationFactory.newApplication(applicationData)).thenReturn(application);
    mockMvc.perform(post("/pages/addChildrenConfirmation/0"));
    assertThat(
        applicationData.getPagesData().getPage("addHouseholdMembers").get("addHouseholdMembers")
            .getValue()).contains("true");
    mockMvc.perform(post("/pages/addChildrenConfirmation/1"));
    assertThat(
        applicationData.getPagesData().getPage("addHouseholdMembers").get("addHouseholdMembers")
            .getValue()).contains("false");
  }

}
