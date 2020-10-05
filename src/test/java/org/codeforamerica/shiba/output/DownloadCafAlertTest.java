package org.codeforamerica.shiba.output;

import org.codeforamerica.shiba.pages.emails.EmailClient;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class DownloadCafAlertTest {
    EmailClient emailClient = mock(EmailClient.class);
    DownloadCafAlert downloadCafAlert = new DownloadCafAlert(emailClient);

    @Test
    void shouldSendEmailWithConfirmationIdAndIp() {
        String confirmationNumber = "abc";
        String ip = "123";

        downloadCafAlert.sendEmail(new DownloadCafEvent(confirmationNumber, ip));

        verify(emailClient).sendDownloadCafAlertEmail(confirmationNumber, ip);
    }
}