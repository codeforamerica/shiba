package org.codeforamerica.shiba.output;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.ApplicationRepository;
import org.codeforamerica.shiba.application.FlowType;
import org.codeforamerica.shiba.application.Status;
import org.codeforamerica.shiba.application.parsers.ContactInfoParser;
import org.codeforamerica.shiba.application.parsers.EmailParser;
import org.codeforamerica.shiba.internationalization.LocaleSpecificMessageSource;
import org.codeforamerica.shiba.pages.DocRecommendationMessageService;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.emails.EmailClient;
import org.codeforamerica.shiba.pages.emails.EmailContentCreator;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DocumentUploadEmailService {

  private final EmailClient emailClient;
  private final EmailContentCreator emailContentCreator;
  private final ApplicationRepository applicationRepository;
  private final MessageSource messageSource;
  private final String senderEmail;
  private final DocRecommendationMessageService docRecommendationMessageService;

  public DocumentUploadEmailService(
      @Value("${sender-email}") String senderEmail,
      EmailClient emailClient,
      EmailContentCreator emailContentCreator,
      ApplicationRepository applicationRepository,
      MessageSource messageSource,
      DocRecommendationMessageService docRecommendationMessageService) {
    this.senderEmail = senderEmail;
    this.emailClient = emailClient;
    this.emailContentCreator = emailContentCreator;
    this.applicationRepository = applicationRepository;
    this.messageSource = messageSource;
    this.docRecommendationMessageService = docRecommendationMessageService;
  }

  /**
   * Sends document upload reminder emails to any applications that
   * - Are not laterdocs apps
   * - were submitted between 48 and 12 hours ago
   * - do not have any uploaded docs
   * - have an email address
   * - have not yet been sent a doc upload email
   * - has document recommendations
   * - opted into email communications
   */
  @Scheduled(cron = "${documentUploadEmails.cronExpression}")
  @SchedulerLock(name = "documentUploadEmails", lockAtMostFor = "30m", lockAtLeastFor = "15m")
  public void sendDocumentUploadEmailReminders() {
    log.info("Checking for applications that need document upload email reminders");
    List<Application> applications = getApplicationsThatNeedDocumentUploadEmails();

    if (applications.isEmpty()) {
      log.info("There are no applications that need document upload email reminders");
      return;
    }

    applications.forEach(this::sendDocumentUploadEmailReminder);
    MDC.clear();
  }

  private List<Application> getApplicationsThatNeedDocumentUploadEmails() {
    List<Application> appsFromTheLastTwoDays =
        applicationRepository.getApplicationsSubmittedBetweenTimestamps(
            Timestamp.from(Instant.now().minus(Duration.ofHours(48))),
            Timestamp.from(Instant.now().minus(Duration.ofHours(12))));

    return appsFromTheLastTwoDays.stream()
        .filter(a -> !a.getFlow().equals(FlowType.LATER_DOCS))
        .filter(a -> a.getApplicationData().getUploadedDocs().isEmpty())
        .filter(a -> a.getDocUploadEmailStatus() == null)
        .filter(a -> !docRecommendationMessageService.getConfirmationEmailDocumentRecommendations(
            a.getApplicationData(), a.getApplicationData().getLocale()).isEmpty())
        .filter(a -> ContactInfoParser.optedIntoEmailCommunications(a.getApplicationData()))
        .toList();
  }

  private void sendDocumentUploadEmailReminder(Application app) {
    String id = app.getId();
    MDC.put("applicationId", id);
    ApplicationData applicationData = app.getApplicationData();

    EmailParser.parse(applicationData).ifPresent(clientEmail -> {
          try {
            log.info("Attempting to send document upload email reminder for application %s".formatted(id));
            Locale locale = applicationData.getLocale();
            String emailContent = emailContentCreator.createDocRecommendationEmail(applicationData);
            LocaleSpecificMessageSource lms = new LocaleSpecificMessageSource(locale, messageSource);
            String subject = lms.getMessage("email.document-recommendation-email-subject");

            emailClient.sendEmail(subject, senderEmail, clientEmail, emailContent, id);
            applicationRepository.setDocUploadEmailStatus(id, Status.DELIVERED);
          } catch (Exception e) {
            log.error("Failed to send document upload email for application %s".formatted(id), e);
            applicationRepository.setDocUploadEmailStatus(id, Status.DELIVERY_FAILED);
          }
        }
    );
  }
}
