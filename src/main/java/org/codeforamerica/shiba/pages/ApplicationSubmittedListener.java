package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.ApplicationRepository;
import org.codeforamerica.shiba.output.MnitDocumentConsumer;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class ApplicationSubmittedListener {
    private final MnitDocumentConsumer mnitDocumentConsumer;
    private final ApplicationRepository applicationRepository;

    public ApplicationSubmittedListener(MnitDocumentConsumer mnitDocumentConsumer,
                                        ApplicationRepository applicationRepository) {
        this.mnitDocumentConsumer = mnitDocumentConsumer;
        this.applicationRepository = applicationRepository;
    }

    @Async
    @EventListener
    public void handleApplicationSubmittedEvent(ApplicationSubmittedEvent applicationSubmittedEvent) {
        //TODO: put this back when MN-IT integration is done
//        this.mnitDocumentConsumer.process(this.applicationRepository.find(applicationSubmittedEvent.getApplicationId()));
    }
}
