package org.codeforamerica.shiba;

import org.codeforamerica.shiba.application.ApplicationRepository;
import org.codeforamerica.shiba.application.Status;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.springframework.stereotype.Component;

import static org.codeforamerica.shiba.application.ApplicationStatusType.*;

@Component
public class ApplicationStatusUpdater {
    private final ApplicationData applicationData;
    private final ApplicationRepository applicationRepository;


    public ApplicationStatusUpdater(ApplicationData applicationData,
                                    ApplicationRepository applicationRepository) {
        this.applicationData = applicationData;
        this.applicationRepository = applicationRepository;
    }

    public void updateUploadedDocumentsStatus(Status status) {
        applicationData.setUploadedDocumentsStatus(status);
        applicationRepository.updateStatus(applicationData.getId(), UPLOADED_DOCUMENTS, status);
    }

    public void updateCafApplicationStatus(Status status) {
        applicationData.setCafApplicationStatus(status);
        applicationRepository.updateStatus(applicationData.getId(), CAF, status);
    }

    public void updateCcapApplicationStatus(Status status) {
        applicationData.setCcapApplicationStatus(status);
        applicationRepository.updateStatus(applicationData.getId(), CCAP, status);
    }
}
