package org.codeforamerica.shiba.pages.events;

import lombok.extern.slf4j.Slf4j;

import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.MonitoringService;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.ApplicationRepository;
import org.codeforamerica.shiba.output.MnitDocumentConsumer;
import org.codeforamerica.shiba.pages.config.FeatureFlagConfiguration;
import org.codeforamerica.shiba.pages.emails.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UploadedDocumentsSubmittedListener {
    private final MnitDocumentConsumer mnitDocumentConsumer;
    private final ApplicationRepository applicationRepository;
    private final MonitoringService monitoringService;
    private final FeatureFlagConfiguration featureFlags;
    private final EmailClient emailClient;

    public UploadedDocumentsSubmittedListener(MnitDocumentConsumer mnitDocumentConsumer,
                                              ApplicationRepository applicationRepository,
                                              MonitoringService monitoringService,
                                              FeatureFlagConfiguration featureFlags,
                                              EmailClient emailClient) {
        this.mnitDocumentConsumer = mnitDocumentConsumer;
        this.applicationRepository = applicationRepository;
        this.monitoringService = monitoringService;
        this.featureFlags = featureFlags;
        this.emailClient = emailClient;
    }

    @Async
    @EventListener
    public void send(UploadedDocumentsSubmittedEvent event) {
        if (featureFlags.get("document-upload-feature").isOn()) {
            Application application = getApplicationFromEvent(event);
            if (featureFlags.get("submit-docs-via-email-for-hennepin").isOn()
                    && (application.getCounty().equals(County.Hennepin)
                        || application.getCounty().equals(County.Other)) ) {
                log.info("Processing Hennepin uploaded documents");
            	emailClient.sendHennepinDocUploadsEmail(application);
            } else {
                log.info("Processing uploaded documents");
            	mnitDocumentConsumer.processUploadedDocuments(application);
            }
        }
    }

    @NotNull
    private Application getApplicationFromEvent(UploadedDocumentsSubmittedEvent event) {
        Application application = applicationRepository.find(event.getApplicationId());
        monitoringService.setApplicationId(application.getId());
        return application;
    }
}
