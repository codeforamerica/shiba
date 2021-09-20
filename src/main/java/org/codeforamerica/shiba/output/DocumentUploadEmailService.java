package org.codeforamerica.shiba.output;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import lombok.extern.slf4j.Slf4j;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.ApplicationRepository;
import org.codeforamerica.shiba.application.Status;
import org.codeforamerica.shiba.internationalization.LocaleSpecificMessageSource;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.emails.EmailClient;
import org.codeforamerica.shiba.pages.emails.EmailContentCreator;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DocumentUploadEmailService {

  private final EmailClient emailClient;
  private final EmailContentCreator emailContentCreator;
  private final ApplicationRepository applicationRepository;
  private final MessageSource messageSource;

  // TODO add test
  public DocumentUploadEmailService(
      EmailClient emailClient,
      EmailContentCreator emailContentCreator,
      ApplicationRepository applicationRepository,
      MessageSource messageSource) {
    this.emailClient = emailClient;
    this.emailContentCreator = emailContentCreator;
    this.applicationRepository = applicationRepository;
    this.messageSource = messageSource;
  }

  //  @Scheduled(cron = "0 0 15 * * *") // at 15:00 UTC (10:00 CT) each day
//  @SchedulerLock(name = "documentUploadEmails", lockAtMostFor = "30m")
  public void sendDocumentUploadEmails() {
    log.info("Sending document upload emails");
    List<Application> applicationsThatNeedDocumentEmails = applicationRepository.getApplicationsThatNeedDocumentEmails();
    // find all NON-LATERDOCS applications from more than 12 hours ago that have doc_upload_email_status set to in_progress

    applicationsThatNeedDocumentEmails.forEach(application -> {
      ApplicationData applicationData = application.getApplicationData();
      Locale locale = applicationData.getLocale();
      String emailContent = emailContentCreator.createDocumentRecommendationEmail(
          applicationData, locale);

      LocaleSpecificMessageSource lms = new LocaleSpecificMessageSource(locale, messageSource);

      emailClient.sendEmail(lms.getMessage("email.document-recommendation-email-subject"),
          "someone",
          "someone",
          emailContent,
          Collections.emptyList());
      applicationRepository.setDocUploadEmailStatus(application.getId(), Status.DELIVERED);
    });
    // create the doc upload emails with emailContentCreator
    // send the doc upload emails with mailgun email client
    // if it succeeds set the status to delivered
  }
}
