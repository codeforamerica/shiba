package org.codeforamerica.shiba.output;

import org.codeforamerica.shiba.pages.emails.EmailClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
    value = "feature-flag.send-download-alert",
    havingValue = "on"
)
public class DownloadCafAlert {

  private final EmailClient emailClient;

  public DownloadCafAlert(EmailClient emailClient) {
    this.emailClient = emailClient;
  }

  @Async
  @EventListener
  public void sendEmail(DownloadCafEvent event) {
    emailClient.sendDownloadCafAlertEmail(event.getConfirmationNumber(), event.getIp(),
        LocaleContextHolder.getLocale());
  }
}
