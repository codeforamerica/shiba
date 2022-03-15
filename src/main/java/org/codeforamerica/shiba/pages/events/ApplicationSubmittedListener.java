package org.codeforamerica.shiba.pages.events;

import static org.codeforamerica.shiba.output.Recipient.CLIENT;

import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.codeforamerica.shiba.MonitoringService;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.ApplicationRepository;
import org.codeforamerica.shiba.application.parsers.ContactInfoParser;
import org.codeforamerica.shiba.application.parsers.DocumentListParser;
import org.codeforamerica.shiba.application.parsers.EmailParser;
import org.codeforamerica.shiba.output.ApplicationFile;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.MnitDocumentConsumer;
import org.codeforamerica.shiba.output.caf.CcapExpeditedEligibility;
import org.codeforamerica.shiba.output.caf.CcapExpeditedEligibilityDecider;
import org.codeforamerica.shiba.output.caf.SnapExpeditedEligibility;
import org.codeforamerica.shiba.output.caf.SnapExpeditedEligibilityDecider;
import org.codeforamerica.shiba.output.pdf.PdfGenerator;
import org.codeforamerica.shiba.pages.config.FeatureFlagConfiguration;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.emails.EmailClient;
import org.slf4j.MDC;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ApplicationSubmittedListener extends ApplicationEventListener {

  private final MnitDocumentConsumer mnitDocumentConsumer;
  private final EmailClient emailClient;
  private final SnapExpeditedEligibilityDecider snapExpeditedEligibilityDecider;
  private final CcapExpeditedEligibilityDecider ccapExpeditedEligibilityDecider;
  private final PdfGenerator pdfGenerator;
  private final FeatureFlagConfiguration featureFlags;

  public ApplicationSubmittedListener(MnitDocumentConsumer mnitDocumentConsumer,
      ApplicationRepository applicationRepository,
      EmailClient emailClient,
      SnapExpeditedEligibilityDecider snapExpeditedEligibilityDecider,
      CcapExpeditedEligibilityDecider ccapExpeditedEligibilityDecider,
      PdfGenerator pdfGenerator,
      FeatureFlagConfiguration featureFlagConfiguration,
      MonitoringService monitoringService) {
    super(applicationRepository, monitoringService);
    this.mnitDocumentConsumer = mnitDocumentConsumer;
    this.emailClient = emailClient;
    this.snapExpeditedEligibilityDecider = snapExpeditedEligibilityDecider;
    this.ccapExpeditedEligibilityDecider = ccapExpeditedEligibilityDecider;
    this.pdfGenerator = pdfGenerator;
    this.featureFlags = featureFlagConfiguration;
  }

  @Async
  @EventListener
  public void sendViaApi(ApplicationSubmittedEvent event) {
    log.info("sendViaApi received ApplicationSubmittedEvent with application ID: "
        + event.getApplicationId());
    if (featureFlags.get("submit-via-api").isOn()) {
      Application application = getApplicationFromEvent(event);
      logTimeSinceCompleted(application);
      mnitDocumentConsumer.processCafAndCcap(application);
    }
    MDC.clear();
  }

  @Async
  @EventListener
  public void sendConfirmationEmail(ApplicationSubmittedEvent event) {
    Application application = getApplicationFromEvent(event);
    ApplicationData applicationData = application.getApplicationData();

    EmailParser.parse(applicationData).ifPresent(email -> {
      String applicationId = application.getId();
      SnapExpeditedEligibility snapExpeditedEligibility =
          snapExpeditedEligibilityDecider.decide(applicationData);
      CcapExpeditedEligibility ccapExpeditedEligibility =
          ccapExpeditedEligibilityDecider.decide(applicationData);
      List<Document> docs = DocumentListParser.parse(applicationData);
      List<ApplicationFile> pdfs = docs.stream()
          .map(doc -> pdfGenerator.generate(applicationId, doc, CLIENT)).toList();

      if (ContactInfoParser.optedIntoEmailCommunications(applicationData)) {
        emailClient.sendShortConfirmationEmail(applicationData, email, applicationId,
            new ArrayList<>(applicationData.getApplicantAndHouseholdMemberPrograms()),
            snapExpeditedEligibility, ccapExpeditedEligibility, pdfs, event.getLocale());
        emailClient.sendNextStepsEmail(applicationData, email, applicationId,
            new ArrayList<>(applicationData.getApplicantAndHouseholdMemberPrograms()),
            snapExpeditedEligibility, ccapExpeditedEligibility, pdfs, event.getLocale());
      } else {
        emailClient.sendConfirmationEmail(applicationData, email, applicationId,
            new ArrayList<>(applicationData.getApplicantAndHouseholdMemberPrograms()),
            snapExpeditedEligibility, ccapExpeditedEligibility, pdfs, event.getLocale());
      }
    });
    MDC.clear();
  }
}
