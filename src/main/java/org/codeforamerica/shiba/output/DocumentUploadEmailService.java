package org.codeforamerica.shiba.output;

import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.codeforamerica.shiba.pages.emails.EmailContentCreator;
import org.codeforamerica.shiba.pages.emails.MailGunEmailClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DocumentUploadEmailService {

  private final MailGunEmailClient mailGunEmailClient;
  private final EmailContentCreator emailContentCreator;

  // TODO add test
  public DocumentUploadEmailService(
      MailGunEmailClient mailGunEmailClient,
      EmailContentCreator emailContentCreator) {
    this.mailGunEmailClient = mailGunEmailClient;
    this.emailContentCreator = emailContentCreator;
  }

  @Scheduled(cron = "0 0 15 * * *") // at 15:00 UTC (10:00 CT) each day
  @SchedulerLock(name = "documentUploadEmails", lockAtMostFor = "30m")
  public void sendDocumentUploadEmails() {
    // find all NON-LATERDOCS applications from more than 12 hours ago that have doc_upload_email_status set to in_progress
    // create the doc upload emails with emailContentCreator
    // send the doc upload emails with mailgun email client
    // if it succeeds set the status to delivered
  }
}
