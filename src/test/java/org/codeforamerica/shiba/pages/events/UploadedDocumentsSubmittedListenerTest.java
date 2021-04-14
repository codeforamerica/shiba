package org.codeforamerica.shiba.pages.events;

import org.codeforamerica.shiba.MonitoringService;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.ApplicationRepository;
import org.codeforamerica.shiba.output.MnitDocumentConsumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UploadedDocumentsSubmittedListenerTest {
    @Mock
    private MnitDocumentConsumer mnitDocumentConsumer;
    @Mock
    private ApplicationRepository applicationRepository;
    @Mock
    private MonitoringService monitoringService;

    private UploadedDocumentsSubmittedListener uploadedDocumentsSubmittedListener;

    @BeforeEach
    void setUp() {
        uploadedDocumentsSubmittedListener = new UploadedDocumentsSubmittedListener(
                mnitDocumentConsumer,
                applicationRepository,
                monitoringService);
    }

    @Test
    void sendViaApi() {
        String sessionId = "some-session-id";
        String applicationId = "some-application-id";
        Application application = Application.builder().id(applicationId).build();
        Mockito.when(applicationRepository.find(eq(applicationId))).thenReturn(application);

        UploadedDocumentsSubmittedEvent event = new UploadedDocumentsSubmittedEvent(sessionId, applicationId);
        uploadedDocumentsSubmittedListener.sendViaApi(event);

        verify(mnitDocumentConsumer).processUploadedDocuments(application);
    }
}