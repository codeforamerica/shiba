package org.codeforamerica.shiba.output;

import org.codeforamerica.shiba.pages.EmailClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        value = "send-download-alert",
        havingValue = "true"
)
public class DownloadCafAlert {
    private final EmailClient emailClient;

    public DownloadCafAlert(EmailClient emailClient) {
        this.emailClient = emailClient;
    }

    @Async
    @EventListener
    public void sendEmail(DownloadCafEvent event) {
        emailClient.sendDownloadCafAlertEmail(event.getConfirmationNumber(), event.getIp());
    }
}
