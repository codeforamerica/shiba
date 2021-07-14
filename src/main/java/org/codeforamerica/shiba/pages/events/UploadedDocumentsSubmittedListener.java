package org.codeforamerica.shiba.pages.events;

import lombok.extern.slf4j.Slf4j;
import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.MonitoringService;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.ApplicationRepository;
import org.codeforamerica.shiba.application.FlowType;
import org.codeforamerica.shiba.application.parsers.EmailParser;
import org.codeforamerica.shiba.output.MnitDocumentConsumer;
import org.codeforamerica.shiba.pages.config.FeatureFlagConfiguration;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.emails.EmailClient;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class UploadedDocumentsSubmittedListener extends ApplicationEventListener {
    private final MnitDocumentConsumer mnitDocumentConsumer;
    private final FeatureFlagConfiguration featureFlags;
    private final EmailClient emailClient;

    public UploadedDocumentsSubmittedListener(MnitDocumentConsumer mnitDocumentConsumer,
                                              ApplicationRepository applicationRepository,
                                              MonitoringService monitoringService,
                                              FeatureFlagConfiguration featureFlags,
                                              EmailClient emailClient) {
        super(applicationRepository, monitoringService);
        this.mnitDocumentConsumer = mnitDocumentConsumer;
        this.featureFlags = featureFlags;
        this.emailClient = emailClient;
    }

    @Async
    @EventListener
    public void send(UploadedDocumentsSubmittedEvent event) {
        Application application = getApplicationFromEvent(event);
        if (featureFlags.get("submit-docs-via-email-for-hennepin").isOn()
                && (application.getCounty().equals(County.Hennepin)
                || application.getCounty().equals(County.Other))) {
            log.info("Processing Hennepin uploaded documents");
            emailClient.sendHennepinDocUploadsEmails(application);
        } else {
            log.info("Processing uploaded documents");
            mnitDocumentConsumer.processUploadedDocuments(application);
        }
    }

    @Async
    @EventListener
    public void sendConfirmationEmail(UploadedDocumentsSubmittedEvent event) {
        Application application = getApplicationFromEvent(event);
        if (application.getFlow() == FlowType.LATER_DOCS) {
            sendLaterDocsConfirmationEmail(event);
        }
    }

    private void sendLaterDocsConfirmationEmail(UploadedDocumentsSubmittedEvent event) {
        Application application = getApplicationFromEvent(event);
        ApplicationData applicationData = application.getApplicationData();

        EmailParser.parse(applicationData)
                .ifPresent(email -> emailClient.sendLaterDocsConfirmationEmail(email, event.getLocale()));
    }
}
