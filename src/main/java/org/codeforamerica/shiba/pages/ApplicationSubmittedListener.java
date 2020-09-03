package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.Application;
import org.codeforamerica.shiba.ApplicationRepository;
import org.codeforamerica.shiba.output.ApplicationFile;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.MnitDocumentConsumer;
import org.codeforamerica.shiba.output.applicationinputsmappers.ApplicationInputsMappers;
import org.codeforamerica.shiba.output.caf.ExpeditedEligibility;
import org.codeforamerica.shiba.output.caf.ExpeditedEligibilityDecider;
import org.codeforamerica.shiba.output.pdf.PdfGenerator;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static org.codeforamerica.shiba.output.Recipient.CLIENT;

@Component
public class ApplicationSubmittedListener {
    private final MnitDocumentConsumer mnitDocumentConsumer;
    private final ApplicationRepository applicationRepository;
    private final EmailClient emailClient;
    private final ExpeditedEligibilityDecider expeditedEligibilityDecider;
    private final ApplicationInputsMappers applicationInputsMappers;
    private final PdfGenerator pdfGenerator;

    public ApplicationSubmittedListener(MnitDocumentConsumer mnitDocumentConsumer,
                                        ApplicationRepository applicationRepository,
                                        EmailClient emailClient,
                                        ExpeditedEligibilityDecider expeditedEligibilityDecider,
                                        ApplicationInputsMappers applicationInputsMappers,
                                        PdfGenerator pdfGenerator) {
        this.mnitDocumentConsumer = mnitDocumentConsumer;
        this.applicationRepository = applicationRepository;
        this.emailClient = emailClient;
        this.expeditedEligibilityDecider = expeditedEligibilityDecider;
        this.applicationInputsMappers = applicationInputsMappers;
        this.pdfGenerator = pdfGenerator;
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
                    List<ApplicationInput> applicationInputs = applicationInputsMappers.map(application, CLIENT);
                    String applicationId = application.getId();
                    ApplicationFile pdf = pdfGenerator.generate(applicationInputs, applicationId);
                    ExpeditedEligibility expeditedEligibility = expeditedEligibilityDecider.decide(pagesData);
                    emailClient.sendConfirmationEmail(input.getValue().get(0), applicationId, expeditedEligibility, pdf);
                });
    }
}
