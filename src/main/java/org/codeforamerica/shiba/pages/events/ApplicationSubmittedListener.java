package org.codeforamerica.shiba.pages.events;

import org.codeforamerica.shiba.CountyMap;
import org.codeforamerica.shiba.MonitoringService;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.ApplicationRepository;
import org.codeforamerica.shiba.application.parsers.DocumentListParser;
import org.codeforamerica.shiba.application.parsers.EmailParser;
import org.codeforamerica.shiba.mnit.MnitCountyInformation;
import org.codeforamerica.shiba.output.ApplicationFile;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.MnitDocumentConsumer;
import org.codeforamerica.shiba.output.caf.*;
import org.codeforamerica.shiba.output.pdf.PdfGenerator;
import org.codeforamerica.shiba.pages.config.FeatureFlagConfiguration;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.pages.emails.EmailClient;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static org.codeforamerica.shiba.output.Document.CAF;
import static org.codeforamerica.shiba.output.Recipient.CASEWORKER;
import static org.codeforamerica.shiba.output.Recipient.CLIENT;

@Component
public class ApplicationSubmittedListener extends ApplicationEventListener {
    private final MnitDocumentConsumer mnitDocumentConsumer;
    private final EmailClient emailClient;
    private final SnapExpeditedEligibilityDecider snapExpeditedEligibilityDecider;
    private final CcapExpeditedEligibilityDecider ccapExpeditedEligibilityDecider;
    private final PdfGenerator pdfGenerator;
    private final CountyMap<MnitCountyInformation> countyMap;
    private final EmailParser emailParser;
    private final FeatureFlagConfiguration featureFlags;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public ApplicationSubmittedListener(MnitDocumentConsumer mnitDocumentConsumer,
                                        ApplicationRepository applicationRepository,
                                        EmailClient emailClient,
                                        SnapExpeditedEligibilityDecider snapExpeditedEligibilityDecider,
                                        CcapExpeditedEligibilityDecider ccapExpeditedEligibilityDecider,
                                        PdfGenerator pdfGenerator,
                                        CountyMap<MnitCountyInformation> countyMap,
                                        FeatureFlagConfiguration featureFlagConfiguration,
                                        EmailParser emailParser,
                                        MonitoringService monitoringService) {
        super(applicationRepository, monitoringService);
        this.mnitDocumentConsumer = mnitDocumentConsumer;
        this.emailClient = emailClient;
        this.snapExpeditedEligibilityDecider = snapExpeditedEligibilityDecider;
        this.ccapExpeditedEligibilityDecider = ccapExpeditedEligibilityDecider;
        this.pdfGenerator = pdfGenerator;
        this.countyMap = countyMap;
        this.featureFlags = featureFlagConfiguration;
        this.emailParser = emailParser;
    }

    @Async
    @EventListener
    public void sendViaApi(ApplicationSubmittedEvent event) {
        if (featureFlags.get("submit-via-api").isOn()) {
            Application application = getApplicationFromEvent(event);
            mnitDocumentConsumer.process(application);
        }
    }

    @Async
    @EventListener
    public void sendConfirmationEmail(ApplicationSubmittedEvent event) {
        Application application = getApplicationFromEvent(event);
        ApplicationData applicationData = application.getApplicationData();

        emailParser.parse(applicationData)
                .ifPresent(email -> {
                    String applicationId = application.getId();
                    SnapExpeditedEligibility snapExpeditedEligibility = snapExpeditedEligibilityDecider.decide(application.getApplicationData());
                    CcapExpeditedEligibility ccapExpeditedEligibility = ccapExpeditedEligibilityDecider.decide(application.getApplicationData());
                    List<Document> docs = DocumentListParser.parse(applicationData);
                    List<ApplicationFile> pdfs = docs.stream().map(doc -> pdfGenerator.generate(applicationId,doc,CLIENT)).collect(Collectors.toList());
                    emailClient.sendConfirmationEmail(email, applicationId, new ArrayList<>(applicationData.getApplicantAndHouseholdMemberPrograms()), snapExpeditedEligibility, ccapExpeditedEligibility, pdfs, event.getLocale());
                });
    }

    @Async
    @EventListener
    public void sendCaseWorkerEmail(ApplicationSubmittedEvent event) {
        if (featureFlags.get("submit-via-email").isOff()) {
            return;
        }

        Application application = getApplicationFromEvent(event);

        PageData personalInfo = application.getApplicationData().getPageData("personalInfo");
        String applicationId = application.getId();
        ApplicationFile pdf = pdfGenerator.generate(applicationId, CAF, CASEWORKER);

        String fullName = String.join(" ", personalInfo.get("firstName").getValue(0), personalInfo.get("lastName").getValue(0));
        emailClient.sendCaseWorkerEmail(countyMap.get(application.getCounty()).getEmail(), fullName, applicationId, pdf);
    }
}
