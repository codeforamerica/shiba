package org.codeforamerica.shiba;

import org.codeforamerica.shiba.application.ApplicationRepository;
import org.codeforamerica.shiba.application.ApplicationStatusType;
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

        applicationStatusUpdater.updateUploadedDocumentsStatus(applicationData.getId(), IN_PROGRESS);
        verify(applicationRepository).updateStatus("testId", ApplicationStatusType.UPLOADED_DOCUMENTS, IN_PROGRESS);

        applicationStatusUpdater.updateUploadedDocumentsStatus(applicationData.getId(), SENDING);
        verify(applicationRepository).updateStatus("testId", ApplicationStatusType.UPLOADED_DOCUMENTS, SENDING);

        applicationStatusUpdater.updateUploadedDocumentsStatus(applicationData.getId(), DELIVERED);
        verify(applicationRepository).updateStatus("testId", ApplicationStatusType.UPLOADED_DOCUMENTS, DELIVERED);

        applicationStatusUpdater.updateUploadedDocumentsStatus(applicationData.getId(), DELIVERY_FAILED);
        verify(applicationRepository).updateStatus("testId", ApplicationStatusType.UPLOADED_DOCUMENTS, DELIVERY_FAILED);
    }

    @Test
    void shouldUpdateCafApplicationStatus() {
        ApplicationStatusUpdater applicationStatusUpdater = new ApplicationStatusUpdater(applicationRepository);

        applicationStatusUpdater.updateCafApplicationStatus(applicationData.getId(), IN_PROGRESS);
        verify(applicationRepository).updateStatus("testId", ApplicationStatusType.CAF, IN_PROGRESS);

        applicationStatusUpdater.updateCafApplicationStatus(applicationData.getId(), SENDING);
        verify(applicationRepository).updateStatus("testId", ApplicationStatusType.CAF, SENDING);

        applicationStatusUpdater.updateCafApplicationStatus(applicationData.getId(), DELIVERED);
        verify(applicationRepository).updateStatus("testId", ApplicationStatusType.CAF, DELIVERED);

        applicationStatusUpdater.updateCafApplicationStatus(applicationData.getId(), DELIVERY_FAILED);
        verify(applicationRepository).updateStatus("testId", ApplicationStatusType.CAF, DELIVERY_FAILED);
    }

    @Test
    void shouldUpdateCcapApplicationStatus() {
        ApplicationStatusUpdater applicationStatusUpdater = new ApplicationStatusUpdater(applicationRepository);

        applicationStatusUpdater.updateCcapApplicationStatus(applicationData.getId(), IN_PROGRESS);
        verify(applicationRepository).updateStatus("testId", ApplicationStatusType.CCAP, IN_PROGRESS);

        applicationStatusUpdater.updateCcapApplicationStatus(applicationData.getId(), SENDING);
        verify(applicationRepository).updateStatus("testId", ApplicationStatusType.CCAP, SENDING);

        applicationStatusUpdater.updateCcapApplicationStatus(applicationData.getId(), DELIVERED);
        verify(applicationRepository).updateStatus("testId", ApplicationStatusType.CCAP, DELIVERED);

        applicationStatusUpdater.updateCcapApplicationStatus(applicationData.getId(), DELIVERY_FAILED);
        verify(applicationRepository).updateStatus("testId", ApplicationStatusType.CCAP, DELIVERY_FAILED);
    }
}