package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.MnitCountyInformation;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.ApplicationRepository;
import org.codeforamerica.shiba.application.parsers.EmailParser;
import org.codeforamerica.shiba.output.ApplicationFile;
import org.codeforamerica.shiba.output.MnitDocumentConsumer;
import org.codeforamerica.shiba.output.caf.ExpeditedEligibility;
import org.codeforamerica.shiba.output.caf.ExpeditedEligibilityDecider;
import org.codeforamerica.shiba.output.pdf.PdfGenerator;
import org.codeforamerica.shiba.pages.data.PageData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import static org.codeforamerica.shiba.output.Recipient.CASEWORKER;
import static org.codeforamerica.shiba.output.Recipient.CLIENT;

@Component
public class ApplicationSubmittedListener {
    private final MnitDocumentConsumer mnitDocumentConsumer;
    private final ApplicationRepository applicationRepository;
    private final EmailClient emailClient;
    private final ExpeditedEligibilityDecider expeditedEligibilityDecider;
    private final PdfGenerator pdfGenerator;
    private final CountyMap<MnitCountyInformation> countyMap;
    private final boolean sendCaseWorkerEmail;
    private final boolean submitViaApi;
    private final Boolean sendNonPartnerCountyAlertEmail;
    private final EmailParser emailParser;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public ApplicationSubmittedListener(MnitDocumentConsumer mnitDocumentConsumer,
                                        ApplicationRepository applicationRepository,
                                        EmailClient emailClient,
                                        ExpeditedEligibilityDecider expeditedEligibilityDecider,
                                        PdfGenerator pdfGenerator,
                                        CountyMap<MnitCountyInformation> countyMap,
                                        @Value("${submit-via-email}") boolean sendCaseWorkerEmail,
                                        @Value("${submit-via-api}") Boolean submitViaApi,
                                        @Value("${send-non-partner-county-alert}") Boolean sendNonPartnerCountyAlertEmail,
                                        EmailParser emailParser) {
        this.mnitDocumentConsumer = mnitDocumentConsumer;
        this.applicationRepository = applicationRepository;
        this.emailClient = emailClient;
        this.expeditedEligibilityDecider = expeditedEligibilityDecider;
        this.pdfGenerator = pdfGenerator;
        this.countyMap = countyMap;
        this.sendCaseWorkerEmail = sendCaseWorkerEmail;
        this.submitViaApi = submitViaApi;
        this.sendNonPartnerCountyAlertEmail = sendNonPartnerCountyAlertEmail;
        this.emailParser = emailParser;
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

        emailParser.parse(application.getApplicationData())
                .ifPresent(email -> {
                    String applicationId = application.getId();
                    ApplicationFile pdf = pdfGenerator.generate(applicationId, CLIENT);
                    ExpeditedEligibility expeditedEligibility = expeditedEligibilityDecider.decide(application.getApplicationData());
                    emailClient.sendConfirmationEmail(email, applicationId, expeditedEligibility, pdf);
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
        emailClient.sendCaseWorkerEmail(countyMap.get(application.getCounty()).getEmail(), fullName, applicationId, pdf);
    }

    @Async
    @EventListener
    public void sendNonPartnerCountyAlert(ApplicationSubmittedEvent event) {
        if (!sendNonPartnerCountyAlertEmail) {
            return;
        }

        Application application = applicationRepository.find(event.getApplicationId());

        if (application.getCounty() == County.OTHER) {
            emailClient.sendNonPartnerCountyAlert(application.getId(), application.getCompletedAt());
        }
    }
}
