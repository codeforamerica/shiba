package org.codeforamerica.shiba.output;

import io.sentry.Sentry;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.mnit.MnitEsbWebServiceClient;
import org.codeforamerica.shiba.output.pdf.PdfGenerator;
import org.codeforamerica.shiba.output.xml.XmlGenerator;
import org.springframework.stereotype.Component;

import static org.codeforamerica.shiba.output.Document.CAF;
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
        Sentry.configureScope(scope -> scope.setContexts("applicationId", application.getId()));

        application.getDocuments().forEach(documentType -> mnitClient.send(
                pdfGenerator.generate(application.getId(), documentType, CASEWORKER), application.getCounty())
        );
        mnitClient.send(xmlGenerator.generate(application.getId(), CAF, CASEWORKER), application.getCounty());
    }
}
