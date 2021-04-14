package org.codeforamerica.shiba.pages.events;

import org.codeforamerica.shiba.MonitoringService;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.ApplicationRepository;
import org.codeforamerica.shiba.output.MnitDocumentConsumer;
import org.codeforamerica.shiba.pages.config.FeatureFlag;
import org.codeforamerica.shiba.pages.config.FeatureFlagConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UploadedDocumentsSubmittedListenerTest {
    @Mock
    private MnitDocumentConsumer mnitDocumentConsumer;
    @Mock
    private ApplicationRepository applicationRepository;
    @Mock
    private MonitoringService monitoringService;
    @Mock
    private FeatureFlagConfiguration featureFlags;

    private UploadedDocumentsSubmittedListener uploadedDocumentsSubmittedListener;
    private String applicationId;
    private Application application;
    private UploadedDocumentsSubmittedEvent event;

    @BeforeEach
    void setUp() {
        String sessionId = "some-session-id";
        applicationId = "some-application-id";
        event = new UploadedDocumentsSubmittedEvent(sessionId, applicationId);

        application = Application.builder().id(applicationId).build();
        uploadedDocumentsSubmittedListener = new UploadedDocumentsSubmittedListener(
                mnitDocumentConsumer,
                applicationRepository,
                monitoringService, featureFlags);
    }

    @Test
    void shouldSendViaApiWhenFeatureFlagIsEnabled() {
        when(applicationRepository.find(eq(applicationId))).thenReturn(application);
        when(featureFlags.get("document-upload-feature")).thenReturn(FeatureFlag.ON);

        uploadedDocumentsSubmittedListener.sendViaApi(event);

        verify(mnitDocumentConsumer).processUploadedDocuments(application);
    }

    @Test
    void shouldNotSendViaApiWhenFeatureFlagIsDisabled() {
        when(featureFlags.get("document-upload-feature")).thenReturn(FeatureFlag.OFF);

        uploadedDocumentsSubmittedListener.sendViaApi(event);

        verifyNoInteractions(mnitDocumentConsumer);
    }
}