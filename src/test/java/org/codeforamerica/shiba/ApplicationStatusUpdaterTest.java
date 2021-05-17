package org.codeforamerica.shiba;

import org.codeforamerica.shiba.application.ApplicationRepository;
import org.codeforamerica.shiba.application.ApplicationStatusType;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
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
        ApplicationStatusUpdater applicationStatusUpdater = new ApplicationStatusUpdater(applicationData, applicationRepository);

        applicationStatusUpdater.updateUploadedDocumentsStatus(IN_PROGRESS);
        assertThat(applicationData.getUploadedDocumentsStatus()).isEqualTo(IN_PROGRESS);
        verify(applicationRepository).updateStatus("testId", ApplicationStatusType.UPLOADED_DOCUMENTS, IN_PROGRESS);

        applicationStatusUpdater.updateUploadedDocumentsStatus(SENDING);
        assertThat(applicationData.getUploadedDocumentsStatus()).isEqualTo(SENDING);
        verify(applicationRepository).updateStatus("testId", ApplicationStatusType.UPLOADED_DOCUMENTS, SENDING);

        applicationStatusUpdater.updateUploadedDocumentsStatus(DELIVERED);
        assertThat(applicationData.getUploadedDocumentsStatus()).isEqualTo(DELIVERED);
        verify(applicationRepository).updateStatus("testId", ApplicationStatusType.UPLOADED_DOCUMENTS, DELIVERED);

        applicationStatusUpdater.updateUploadedDocumentsStatus(DELIVERY_FAILED);
        assertThat(applicationData.getUploadedDocumentsStatus()).isEqualTo(DELIVERY_FAILED);
        verify(applicationRepository).updateStatus("testId", ApplicationStatusType.UPLOADED_DOCUMENTS, DELIVERY_FAILED);
    }

    @Test
    void shouldUpdateCafApplicationStatus() {
        ApplicationStatusUpdater applicationStatusUpdater = new ApplicationStatusUpdater(applicationData, applicationRepository);

        applicationStatusUpdater.updateCafApplicationStatus(IN_PROGRESS);
        assertThat(applicationData.getCafApplicationStatus()).isEqualTo(IN_PROGRESS);
        verify(applicationRepository).updateStatus("testId", ApplicationStatusType.UPLOADED_DOCUMENTS, IN_PROGRESS);

        applicationStatusUpdater.updateCafApplicationStatus(SENDING);
        assertThat(applicationData.getCafApplicationStatus()).isEqualTo(SENDING);
        verify(applicationRepository).updateStatus("testId", ApplicationStatusType.UPLOADED_DOCUMENTS, SENDING);

        applicationStatusUpdater.updateCafApplicationStatus(DELIVERED);
        assertThat(applicationData.getCafApplicationStatus()).isEqualTo(DELIVERED);
        verify(applicationRepository).updateStatus("testId", ApplicationStatusType.UPLOADED_DOCUMENTS, DELIVERED);

        applicationStatusUpdater.updateCafApplicationStatus(DELIVERY_FAILED);
        assertThat(applicationData.getCafApplicationStatus()).isEqualTo(DELIVERY_FAILED);
        verify(applicationRepository).updateStatus("testId", ApplicationStatusType.UPLOADED_DOCUMENTS, DELIVERY_FAILED);
    }

    @Test
    void shouldUpdateCcapApplicationStatus() {
        ApplicationStatusUpdater applicationStatusUpdater = new ApplicationStatusUpdater(applicationData, applicationRepository);

        applicationStatusUpdater.updateCcapApplicationStatus(IN_PROGRESS);
        assertThat(applicationData.getCcapApplicationStatus()).isEqualTo(IN_PROGRESS);
        verify(applicationRepository).updateStatus("testId", ApplicationStatusType.UPLOADED_DOCUMENTS, IN_PROGRESS);

        applicationStatusUpdater.updateCcapApplicationStatus(SENDING);
        assertThat(applicationData.getCcapApplicationStatus()).isEqualTo(SENDING);
        verify(applicationRepository).updateStatus("testId", ApplicationStatusType.UPLOADED_DOCUMENTS, SENDING);

        applicationStatusUpdater.updateCcapApplicationStatus(DELIVERED);
        assertThat(applicationData.getCcapApplicationStatus()).isEqualTo(DELIVERED);
        verify(applicationRepository).updateStatus("testId", ApplicationStatusType.UPLOADED_DOCUMENTS, DELIVERED);

        applicationStatusUpdater.updateCcapApplicationStatus(DELIVERY_FAILED);
        assertThat(applicationData.getCcapApplicationStatus()).isEqualTo(DELIVERY_FAILED);
        verify(applicationRepository).updateStatus("testId", ApplicationStatusType.UPLOADED_DOCUMENTS, DELIVERY_FAILED);
    }
}