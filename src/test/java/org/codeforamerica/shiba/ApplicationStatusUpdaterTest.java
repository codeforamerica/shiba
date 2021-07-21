package org.codeforamerica.shiba;

import org.codeforamerica.shiba.application.ApplicationRepository;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.codeforamerica.shiba.application.Status.*;
import static org.codeforamerica.shiba.output.Document.*;
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
    void shouldUpdateApplicationStatus() {
        ApplicationStatusUpdater applicationStatusUpdater = new ApplicationStatusUpdater(applicationRepository);

        applicationStatusUpdater.updateStatus(applicationData.getId(), UPLOADED_DOC, IN_PROGRESS);
        verify(applicationRepository).updateStatus("testId", UPLOADED_DOC, IN_PROGRESS);
    }
}