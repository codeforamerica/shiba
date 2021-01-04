package org.codeforamerica.shiba.output;

import io.sentry.Sentry;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.mnit.MnitEsbWebServiceClient;
import org.codeforamerica.shiba.output.pdf.PdfGenerator;
import org.codeforamerica.shiba.output.xml.XmlGenerator;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.codeforamerica.shiba.output.DocumentType.CAF;
import static org.codeforamerica.shiba.output.DocumentType.CCAP;
import static org.codeforamerica.shiba.output.Recipient.CASEWORKER;

@Component
public class MnitDocumentConsumer {

    private final MnitEsbWebServiceClient mnitClient;
    private final XmlGenerator xmlGenerator;
    private final PdfGenerator pdfGenerator;

    public MnitDocumentConsumer(MnitEsbWebServiceClient mnitClient,
                                XmlGenerator xmlGenerator,
                                PdfGenerator pdfGenerator) {
        this.mnitClient = mnitClient;
        this.xmlGenerator = xmlGenerator;
        this.pdfGenerator = pdfGenerator;
    }

    public void process(Application application) {
        Sentry.configureScope(scope -> {
            scope.setContexts("applicationId", application.getId());
        });

        mnitClient.send(pdfGenerator.generate(application.getId(), CAF, CASEWORKER), application.getCounty());
        if (shouldSendCCAP(application)) {
            mnitClient.send(pdfGenerator.generate(application.getId(), CCAP, CASEWORKER), application.getCounty());
        }
        mnitClient.send(xmlGenerator.generate(application.getId(), CAF, CASEWORKER), application.getCounty());
    }

    private boolean shouldSendCCAP(Application application) {
        List<String> applicantPrograms = application.getApplicationData().getPagesData().safeGetPageInputValue("choosePrograms", "programs");
        Boolean applicantHasCCAP = applicantPrograms.contains("CCAP");
        Boolean hasHousehold = application.getApplicationData().getSubworkflows().containsKey("household");
        Boolean householdHasCCAP = false;
        if (hasHousehold) {
            householdHasCCAP = application.getApplicationData().getSubworkflows().get("household").stream()
                    .anyMatch(iteration ->
                            iteration.getPagesData().safeGetPageInputValue("householdMemberInfo", "programs").contains("CCAP"));
        }

        return applicantHasCCAP || householdHasCCAP;
    }

}
