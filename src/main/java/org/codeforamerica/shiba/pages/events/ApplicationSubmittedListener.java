package org.codeforamerica.shiba.pages.events;

import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.CountyMap;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.ApplicationRepository;
import org.codeforamerica.shiba.application.parsers.DocumentListParser;
import org.codeforamerica.shiba.application.parsers.EmailParser;
import org.codeforamerica.shiba.mnit.MnitCountyInformation;
import org.codeforamerica.shiba.output.ApplicationFile;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.MnitDocumentConsumer;
import org.codeforamerica.shiba.output.caf.SnapExpeditedEligibility;
import org.codeforamerica.shiba.output.caf.SnapExpeditedEligibilityDecider;
import org.codeforamerica.shiba.output.pdf.PdfGenerator;
import org.codeforamerica.shiba.pages.config.FeatureFlagConfiguration;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.pages.emails.EmailClient;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static org.codeforamerica.shiba.output.Document.CAF;
import static org.codeforamerica.shiba.output.Recipient.CASEWORKER;
import static org.codeforamerica.shiba.output.Recipient.CLIENT;

@Component
public class ApplicationSubmittedListener {
    private final MnitDocumentConsumer mnitDocumentConsumer;
    private final ApplicationRepository applicationRepository;
    private final EmailClient emailClient;
    private final SnapExpeditedEligibilityDecider snapExpeditedEligibilityDecider;
    private final PdfGenerator pdfGenerator;
    private final CountyMap<MnitCountyInformation> countyMap;
    private final EmailParser emailParser;
    private final DocumentListParser documentListParser;
    private final FeatureFlagConfiguration featureFlags;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public ApplicationSubmittedListener(MnitDocumentConsumer mnitDocumentConsumer,
                                        ApplicationRepository applicationRepository,
                                        EmailClient emailClient,
                                        SnapExpeditedEligibilityDecider snapExpeditedEligibilityDecider,
                                        PdfGenerator pdfGenerator,
                                        CountyMap<MnitCountyInformation> countyMap,
                                        FeatureFlagConfiguration featureFlagConfiguration,
                                        EmailParser emailParser,
                                        DocumentListParser documentListParser) {
        this.mnitDocumentConsumer = mnitDocumentConsumer;
        this.applicationRepository = applicationRepository;
        this.emailClient = emailClient;
        this.snapExpeditedEligibilityDecider = snapExpeditedEligibilityDecider;
        this.pdfGenerator = pdfGenerator;
        this.countyMap = countyMap;
        this.featureFlags = featureFlagConfiguration;
        this.emailParser = emailParser;
        this.documentListParser = documentListParser;
    }

    @Async
    @EventListener
    public void sendViaApi(ApplicationSubmittedEvent applicationSubmittedEvent) {
        if (featureFlags.get("submit-via-api").isOn()) {
            this.mnitDocumentConsumer.process(this.applicationRepository.find(applicationSubmittedEvent.getApplicationId()));
        }
    }

    @Async
    @EventListener
    public void sendConfirmationEmail(ApplicationSubmittedEvent event) {
        Application application = applicationRepository.find(event.getApplicationId());
        ApplicationData applicationData = application.getApplicationData();

        emailParser.parse(applicationData)
                .ifPresent(email -> {
                    String applicationId = application.getId();
                    SnapExpeditedEligibility snapExpeditedEligibility = snapExpeditedEligibilityDecider.decide(application.getApplicationData());
                    List<Document> docs = documentListParser.parse(applicationData);
                    List<ApplicationFile> pdfs = docs.stream().map(doc -> pdfGenerator.generate(applicationId,doc,CLIENT)).collect(Collectors.toList());
                    emailClient.sendConfirmationEmail(email, applicationId, snapExpeditedEligibility, pdfs, event.getLocale());
                });
    }

    @Async
    @EventListener
    public void sendCaseWorkerEmail(ApplicationSubmittedEvent event) {
        if (featureFlags.get("submit-via-email").isOff()) {
            return;
        }

        Application application = applicationRepository.find(event.getApplicationId());
        PageData personalInfo = application.getApplicationData().getPageData("personalInfo");
        String applicationId = application.getId();
        ApplicationFile pdf = pdfGenerator.generate(applicationId, CAF, CASEWORKER);

        String fullName = String.join(" ", personalInfo.get("firstName").getValue(0), personalInfo.get("lastName").getValue(0));
        emailClient.sendCaseWorkerEmail(countyMap.get(application.getCounty()).getEmail(), fullName, applicationId, pdf);
    }

    @Async
    @EventListener
    public void sendNonPartnerCountyAlert(ApplicationSubmittedEvent event) {
        if (featureFlags.get("send-non-partner-county-alert").isOff()) {
            return;
        }

        Application application = applicationRepository.find(event.getApplicationId());

        if (application.getCounty() == County.Other) {
            emailClient.sendNonPartnerCountyAlert(application.getId(), application.getCompletedAt());
        }
    }
}
