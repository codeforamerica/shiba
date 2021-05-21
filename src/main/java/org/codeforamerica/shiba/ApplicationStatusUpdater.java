package org.codeforamerica.shiba;

import org.codeforamerica.shiba.application.ApplicationRepository;
import org.codeforamerica.shiba.application.Status;
import org.springframework.stereotype.Component;

import static org.codeforamerica.shiba.application.ApplicationStatusType.*;

@Component
public class ApplicationStatusUpdater {
    private final ApplicationRepository applicationRepository;


    public ApplicationStatusUpdater(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    public void updateUploadedDocumentsStatus(String id, Status status) {
        applicationRepository.updateStatus(id, UPLOADED_DOCUMENTS, status);
    }

    public void updateCafApplicationStatus(String id, Status status) {
        applicationRepository.updateStatus(id, CAF, status);
    }

    public void updateCcapApplicationStatus(String id, Status status) {
        applicationRepository.updateStatus(id, CCAP, status);
    }
}
