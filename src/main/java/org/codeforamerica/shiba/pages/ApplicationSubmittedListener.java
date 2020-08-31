package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.Application;
import org.codeforamerica.shiba.ApplicationRepository;
import org.codeforamerica.shiba.output.MnitDocumentConsumer;
import org.codeforamerica.shiba.output.caf.ExpeditedEligibility;
import org.codeforamerica.shiba.output.caf.ExpeditedEligibilityDecider;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ApplicationSubmittedListener {
    private final MnitDocumentConsumer mnitDocumentConsumer;
    private final ApplicationRepository applicationRepository;
    private final EmailClient emailClient;
    private final ExpeditedEligibilityDecider expeditedEligibilityDecider;

    public ApplicationSubmittedListener(MnitDocumentConsumer mnitDocumentConsumer,
                                        ApplicationRepository applicationRepository,
                                        EmailClient emailClient,
                                        ExpeditedEligibilityDecider expeditedEligibilityDecider) {
        this.mnitDocumentConsumer = mnitDocumentConsumer;
        this.applicationRepository = applicationRepository;
        this.emailClient = emailClient;
        this.expeditedEligibilityDecider = expeditedEligibilityDecider;
    }

    @Async
    @EventListener
    public void handleApplicationSubmittedEvent(ApplicationSubmittedEvent applicationSubmittedEvent) {
        //TODO: put this back when MN-IT integration is done
//        this.mnitDocumentConsumer.process(this.applicationRepository.find(applicationSubmittedEvent.getApplicationId()));
    }

    @Async
    @EventListener
    public void sendEmailForApplication(ApplicationSubmittedEvent event) {
        Application application = applicationRepository.find(event.getApplicationId());
        PagesData pagesData = application.getApplicationData().getPagesData();
        Optional.ofNullable(pagesData
                .getPage("contactInfo")
                .get("email"))
                .ifPresent(input -> {
                    String recipient = input.getValue().get(0);
                    ExpeditedEligibility expeditedEligibility = expeditedEligibilityDecider.decide(pagesData);
                    emailClient.sendConfirmationEmail(recipient, application.getId(), expeditedEligibility);
                });
    }
}
