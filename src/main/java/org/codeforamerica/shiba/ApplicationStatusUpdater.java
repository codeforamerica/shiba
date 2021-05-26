package org.codeforamerica.shiba;

import org.codeforamerica.shiba.application.ApplicationRepository;
import org.codeforamerica.shiba.application.Status;
import org.codeforamerica.shiba.output.Document;
import org.springframework.stereotype.Component;

@Component
public class ApplicationStatusUpdater {
    private final ApplicationRepository applicationRepository;


    public ApplicationStatusUpdater(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    public void updateUploadedDocumentsStatus(String id, Document document, Status status) {
        applicationRepository.updateStatus(id, document, status);
    }

    public void updateCafApplicationStatus(String id, Document document, Status status) {
        applicationRepository.updateStatus(id, document, status);
    }

    public void updateCcapApplicationStatus(String id, Document document, Status status) {
        applicationRepository.updateStatus(id, document, status);
    }
}
