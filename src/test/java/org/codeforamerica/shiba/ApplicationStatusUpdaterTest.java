package org.codeforamerica.shiba;

import org.codeforamerica.shiba.application.ApplicationRepository;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.codeforamerica.shiba.application.Status.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ApplicationStatusUpdaterTest {
    private final ApplicationData applicationData = new ApplicationData();
    private final ApplicationRepository applicationRepository = mock(ApplicationRepository.class);

    @BeforeEach
    void setUp() {
        applicationData.setId("testId");
    }

    @Test
    void shouldUpdateUploadedDocumentsStatus() {
        ApplicationStatusUpdater applicationStatusUpdater = new ApplicationStatusUpdater(applicationRepository);

        applicationStatusUpdater.updateUploadedDocumentsStatus(applicationData.getId(), Document.UPLOADED_DOC, IN_PROGRESS);
        verify(applicationRepository).updateStatus("testId", Document.UPLOADED_DOC, IN_PROGRESS);

        applicationStatusUpdater.updateUploadedDocumentsStatus(applicationData.getId(), Document.UPLOADED_DOC, SENDING);
        verify(applicationRepository).updateStatus("testId", Document.UPLOADED_DOC, SENDING);

        applicationStatusUpdater.updateUploadedDocumentsStatus(applicationData.getId(), Document.UPLOADED_DOC, DELIVERED);
        verify(applicationRepository).updateStatus("testId", Document.UPLOADED_DOC, DELIVERED);

        applicationStatusUpdater.updateUploadedDocumentsStatus(applicationData.getId(), Document.UPLOADED_DOC, DELIVERY_FAILED);
        verify(applicationRepository).updateStatus("testId", Document.UPLOADED_DOC, DELIVERY_FAILED);
    }

    @Test
    void shouldUpdateCafApplicationStatus() {
        ApplicationStatusUpdater applicationStatusUpdater = new ApplicationStatusUpdater(applicationRepository);

        applicationStatusUpdater.updateCafApplicationStatus(applicationData.getId(), Document.CAF, IN_PROGRESS);
        verify(applicationRepository).updateStatus("testId", Document.CAF, IN_PROGRESS);

        applicationStatusUpdater.updateCafApplicationStatus(applicationData.getId(), Document.CAF, SENDING);
        verify(applicationRepository).updateStatus("testId", Document.CAF, SENDING);

        applicationStatusUpdater.updateCafApplicationStatus(applicationData.getId(), Document.CAF, DELIVERED);
        verify(applicationRepository).updateStatus("testId", Document.CAF, DELIVERED);

        applicationStatusUpdater.updateCafApplicationStatus(applicationData.getId(), Document.CAF, DELIVERY_FAILED);
        verify(applicationRepository).updateStatus("testId", Document.CAF, DELIVERY_FAILED);
    }

    @Test
    void shouldUpdateCcapApplicationStatus() {
        ApplicationStatusUpdater applicationStatusUpdater = new ApplicationStatusUpdater(applicationRepository);

        applicationStatusUpdater.updateCcapApplicationStatus(applicationData.getId(), Document.CCAP, IN_PROGRESS);
        verify(applicationRepository).updateStatus("testId", Document.CCAP, IN_PROGRESS);

        applicationStatusUpdater.updateCcapApplicationStatus(applicationData.getId(), Document.CCAP, SENDING);
        verify(applicationRepository).updateStatus("testId", Document.CCAP, SENDING);

        applicationStatusUpdater.updateCcapApplicationStatus(applicationData.getId(), Document.CCAP, DELIVERED);
        verify(applicationRepository).updateStatus("testId", Document.CCAP, DELIVERED);

        applicationStatusUpdater.updateCcapApplicationStatus(applicationData.getId(), Document.CCAP, DELIVERY_FAILED);
        verify(applicationRepository).updateStatus("testId", Document.CCAP, DELIVERY_FAILED);
    }
}