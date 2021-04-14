package org.codeforamerica.shiba.pages.events;

import lombok.extern.slf4j.Slf4j;
import org.codeforamerica.shiba.MonitoringService;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.ApplicationRepository;
import org.codeforamerica.shiba.output.MnitDocumentConsumer;
import org.codeforamerica.shiba.pages.config.FeatureFlagConfiguration;
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

    public UploadedDocumentsSubmittedListener(MnitDocumentConsumer mnitDocumentConsumer, ApplicationRepository applicationRepository, MonitoringService monitoringService, FeatureFlagConfiguration featureFlags) {
        this.mnitDocumentConsumer = mnitDocumentConsumer;
        this.applicationRepository = applicationRepository;
        this.monitoringService = monitoringService;
        this.featureFlags = featureFlags;
    }

    @Async
    @EventListener
    public void sendViaApi(UploadedDocumentsSubmittedEvent event) {
        if (featureFlags.get("document-upload-feature").isOn()) {
            log.info("Processing uploaded documents");
            mnitDocumentConsumer.processUploadedDocuments(getApplicationFromEvent(event));
        }
    }

    @NotNull
    private Application getApplicationFromEvent(UploadedDocumentsSubmittedEvent event) {
        Application application = applicationRepository.find(event.getApplicationId());
        monitoringService.setApplicationId(application.getId());
        return application;
    }
}
