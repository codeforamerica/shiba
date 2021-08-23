package org.codeforamerica.shiba.output;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Locale;
import org.codeforamerica.shiba.pages.emails.EmailClient;
import org.junit.jupiter.api.Test;

class DownloadCafAlertTest {

  EmailClient emailClient = mock(EmailClient.class);
  DownloadCafAlert downloadCafAlert = new DownloadCafAlert(emailClient);

  @Test
  void shouldSendEmailWithConfirmationIdAndIp() {
    String confirmationNumber = "abc";
    String ip = "123";

    downloadCafAlert.sendEmail(new DownloadCafEvent(confirmationNumber, ip));

    verify(emailClient)
        .sendDownloadCafAlertEmail(eq(confirmationNumber), eq(ip), any(Locale.class));
  }
}
