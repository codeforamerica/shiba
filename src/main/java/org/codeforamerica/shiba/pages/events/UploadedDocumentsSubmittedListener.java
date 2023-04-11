package org.codeforamerica.shiba.pages.events;

import java.util.Locale;
import lombok.extern.slf4j.Slf4j;
import org.codeforamerica.shiba.MonitoringService;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.ApplicationRepository;
import org.codeforamerica.shiba.application.FlowType;
import org.codeforamerica.shiba.application.parsers.EmailParser;
import org.codeforamerica.shiba.output.MnitDocumentConsumer;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.emails.EmailClient;
import org.slf4j.MDC;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UploadedDocumentsSubmittedListener extends ApplicationEventListener {

  private final MnitDocumentConsumer mnitDocumentConsumer;
  private final EmailClient emailClient;

  public UploadedDocumentsSubmittedListener(MnitDocumentConsumer mnitDocumentConsumer,
      ApplicationRepository applicationRepository,
      MonitoringService monitoringService,
      EmailClient emailClient) {
    super(applicationRepository, monitoringService);
    this.mnitDocumentConsumer = mnitDocumentConsumer;
    this.emailClient = emailClient;
  }

  @Async
  @EventListener
  public void send(UploadedDocumentsSubmittedEvent event) {
    log.info("Processing uploaded documents for application ID: " + event.getApplicationId());
    Application application = getApplicationFromEvent(event);
    logTimeSinceCompleted(application);
    mnitDocumentConsumer.processUploadedDocuments(application);
    MDC.clear();
  }

  @Async
  @EventListener
  public void sendConfirmationEmail(UploadedDocumentsSubmittedEvent event) {
    Application application = getApplicationFromEvent(event);
    if (application.getFlow() == FlowType.LATER_DOCS) {
      sendLaterDocsConfirmationEmail(application, event.getLocale());
    }
    if (application.getFlow() == FlowType.HEALTHCARE_RENEWAL) {
      sendHealthcareRenewalConfirmationEmail(application, event.getLocale());
    }
    MDC.clear();
  }

  private void sendLaterDocsConfirmationEmail(Application application, Locale locale) {
    ApplicationData applicationData = application.getApplicationData();

    EmailParser.parse(applicationData)
        .ifPresent(email -> emailClient.sendLaterDocsConfirmationEmail(application,  application.getId(), email, locale));
  }
  
  private void sendHealthcareRenewalConfirmationEmail(Application application, Locale locale) {
    ApplicationData applicationData = application.getApplicationData();

    EmailParser.parse(applicationData)
        .ifPresent(email -> emailClient.sendHealthcareRenewalConfirmationEmail(application,  application.getId(), email, locale));
  }
}
