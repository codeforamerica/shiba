package org.codeforamerica.shiba.pages.events;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Locale;
import java.util.Optional;
import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.MonitoringService;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.ApplicationRepository;
import org.codeforamerica.shiba.application.FlowType;
import org.codeforamerica.shiba.application.parsers.EmailParser;
import org.codeforamerica.shiba.output.MnitDocumentConsumer;
import org.codeforamerica.shiba.pages.config.FeatureFlag;
import org.codeforamerica.shiba.pages.config.FeatureFlagConfiguration;
import org.codeforamerica.shiba.pages.emails.EmailClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UploadedDocumentsSubmittedListenerTest {

  @Mock
  private MnitDocumentConsumer mnitDocumentConsumer;
  @Mock
  private ApplicationRepository applicationRepository;
  @Mock
  private MonitoringService monitoringService;
  @Mock
  private FeatureFlagConfiguration featureFlags;
  @Mock
  private EmailClient emailClient;

  private UploadedDocumentsSubmittedListener uploadedDocumentsSubmittedListener;
  private String applicationId;
  private Application application;
  private UploadedDocumentsSubmittedEvent event;
  private Locale locale = new Locale("en");

  @BeforeEach
  void setUp() {
    String sessionId = "some-session-id";
    applicationId = "some-application-id";
    event = new UploadedDocumentsSubmittedEvent(sessionId, applicationId, locale);

    application = Application.builder().id(applicationId).county(County.Olmsted).build();
    uploadedDocumentsSubmittedListener = new UploadedDocumentsSubmittedListener(
        mnitDocumentConsumer,
        applicationRepository,
        monitoringService,
        featureFlags,
        emailClient);
  }

  @Test
  void shouldSendViaApiWhenFeatureFlagIsEnabled() {
    when(applicationRepository.find(eq(applicationId))).thenReturn(application);
    when(featureFlags.get("submit-docs-via-email-for-hennepin")).thenReturn(FeatureFlag.ON);
    uploadedDocumentsSubmittedListener.send(event);

    verify(mnitDocumentConsumer).processUploadedDocuments(application);
  }


  @Test
  void shouldSendViaEmailWhenCountyIsHennepinAndFeatureFlagIsEnabled() {
    Application hennepinApplication = Application.builder().id(applicationId)
        .county(County.Hennepin).build();
    when(applicationRepository.find(eq(applicationId))).thenReturn(hennepinApplication);
    when(featureFlags.get("submit-docs-via-email-for-hennepin")).thenReturn(FeatureFlag.ON);

    uploadedDocumentsSubmittedListener.send(event);

    verify(emailClient).sendHennepinDocUploadsEmails(hennepinApplication);
  }

  @Test
  void shouldSendConfirmationEmail() {
    application = Application.builder().id(applicationId).flow(FlowType.LATER_DOCS).build();
    when(applicationRepository.find(eq(applicationId))).thenReturn(application);
    String email = "confirmation email";
    try (MockedStatic<EmailParser> mockEmailParser = Mockito.mockStatic(EmailParser.class)) {
      mockEmailParser.when(() -> EmailParser.parse(any())).thenReturn(Optional.of(email));
      uploadedDocumentsSubmittedListener.sendConfirmationEmail(event);
    }

    verify(emailClient).sendLaterDocsConfirmationEmail(email, locale);
  }
}
