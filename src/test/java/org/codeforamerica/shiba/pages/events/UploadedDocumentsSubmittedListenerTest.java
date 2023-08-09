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

  private final String applicationId = "some-application-id";
  private final Locale locale = new Locale("en");
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
  private UploadedDocumentsSubmittedEvent event;

  @BeforeEach
  void setUp() {
    String sessionId = "some-session-id";
    event = new UploadedDocumentsSubmittedEvent(sessionId, applicationId, locale);

    uploadedDocumentsSubmittedListener = new UploadedDocumentsSubmittedListener(
        mnitDocumentConsumer,
        applicationRepository,
        monitoringService,
        emailClient);
  }

  @Test
  void shouldSendViaApi() {
    Application application = Application.builder().id(applicationId).county(County.Olmsted)
        .build();
    when(applicationRepository.find(eq(applicationId))).thenReturn(application);

    uploadedDocumentsSubmittedListener.send(event);

    verify(mnitDocumentConsumer).processUploadedDocuments(application);
  }

  @Test
  void shouldSendConfirmationEmail() {
    Application application = Application.builder().id(applicationId).flow(FlowType.LATER_DOCS)
        .build();
    when(applicationRepository.find(eq(applicationId))).thenReturn(application);
    String email = "confirmation email";
    try (MockedStatic<EmailParser> mockEmailParser = Mockito.mockStatic(EmailParser.class)) {
      mockEmailParser.when(() -> EmailParser.parse(any())).thenReturn(Optional.of(email));
      uploadedDocumentsSubmittedListener.sendConfirmationEmail(event);
    }

    verify(emailClient).sendLaterDocsConfirmationEmail(application, applicationId, email, locale);
  }
  
  @Test
  void shouldSendConfirmationEmailHealthcareRenewal() {
    Application application = Application.builder().id(applicationId).flow(FlowType.HEALTHCARE_RENEWAL)
        .build();
    when(applicationRepository.find(eq(applicationId))).thenReturn(application);
    String email = "confirmation email";
    try (MockedStatic<EmailParser> mockEmailParser = Mockito.mockStatic(EmailParser.class)) {
      mockEmailParser.when(() -> EmailParser.parse(any())).thenReturn(Optional.of(email));
      uploadedDocumentsSubmittedListener.sendConfirmationEmail(event);
    }

    verify(emailClient).sendHealthcareRenewalConfirmationEmail(application, applicationId, email, locale);
  }
}
