package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.Application;
import org.codeforamerica.shiba.ApplicationRepository;
import org.codeforamerica.shiba.output.ApplicationFile;
import org.codeforamerica.shiba.output.MnitDocumentConsumer;
import org.codeforamerica.shiba.output.caf.ExpeditedEligibility;
import org.codeforamerica.shiba.output.caf.ExpeditedEligibilityDecider;
import org.codeforamerica.shiba.output.pdf.PdfGenerator;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static org.codeforamerica.shiba.output.Recipient.CASEWORKER;
import static org.codeforamerica.shiba.output.Recipient.CLIENT;

@Component
public class ApplicationSubmittedListener {
    private final MnitDocumentConsumer mnitDocumentConsumer;
    private final ApplicationRepository applicationRepository;
    private final EmailClient emailClient;
    private final ExpeditedEligibilityDecider expeditedEligibilityDecider;
    private final PdfGenerator pdfGenerator;
    private final CountyEmailMap countyEmailMap;
    private final boolean sendCaseWorkerEmail;
    private final boolean submitViaApi;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public ApplicationSubmittedListener(MnitDocumentConsumer mnitDocumentConsumer,
                                        ApplicationRepository applicationRepository,
                                        EmailClient emailClient,
                                        ExpeditedEligibilityDecider expeditedEligibilityDecider,
                                        PdfGenerator pdfGenerator,
                                        CountyEmailMap countyEmailMap,
                                        @Value("${submit-via-email}") boolean sendCaseWorkerEmail,
                                        @Value("${submit-via-api}") Boolean submitViaApi) {
        this.mnitDocumentConsumer = mnitDocumentConsumer;
        this.applicationRepository = applicationRepository;
        this.emailClient = emailClient;
        this.expeditedEligibilityDecider = expeditedEligibilityDecider;
        this.pdfGenerator = pdfGenerator;
        this.countyEmailMap = countyEmailMap;
        this.sendCaseWorkerEmail = sendCaseWorkerEmail;
        this.submitViaApi = submitViaApi;
    }

    @Async
    @EventListener
    public void sendViaApi(ApplicationSubmittedEvent applicationSubmittedEvent) {
        if (submitViaApi) {
            this.mnitDocumentConsumer.process(this.applicationRepository.find(applicationSubmittedEvent.getApplicationId()));
        }
    }

    @Async
    @EventListener
    public void sendConfirmationEmail(ApplicationSubmittedEvent event) {
        Application application = applicationRepository.find(event.getApplicationId());
        PagesData pagesData = application.getApplicationData().getPagesData();
        Optional.ofNullable(pagesData
                .getPage("contactInfo")
                .get("email"))
                .ifPresent(input -> {
                    String applicationId = application.getId();
                    ApplicationFile pdf = pdfGenerator.generate(applicationId, CLIENT);
                    ExpeditedEligibility expeditedEligibility = expeditedEligibilityDecider.decide(pagesData);
                    emailClient.sendConfirmationEmail(input.getValue().get(0), applicationId, expeditedEligibility, pdf);
                });
    }

    @Async
    @EventListener
    public void sendCaseWorkerEmail(ApplicationSubmittedEvent event) {
        if (!sendCaseWorkerEmail) {
            return;
        }

        Application application = applicationRepository.find(event.getApplicationId());
        PageData personalInfo = application.getApplicationData().getInputDataMap("personalInfo");
        String applicationId = application.getId();
        ApplicationFile pdf = pdfGenerator.generate(applicationId, CASEWORKER);

        String fullName = String.join(" ", personalInfo.get("firstName").getValue().get(0), personalInfo.get("lastName").getValue().get(0));
        emailClient.sendCaseWorkerEmail(countyEmailMap.get(application.getCounty()), fullName, pdf);
    }
}
