package org.codeforamerica.shiba.output;

import io.sentry.Sentry;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.parsers.ApplicationDataParser;
import org.codeforamerica.shiba.mnit.MnitEsbWebServiceClient;
import org.codeforamerica.shiba.output.pdf.PdfGenerator;
import org.codeforamerica.shiba.output.xml.XmlGenerator;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.codeforamerica.shiba.output.Document.CAF;
import static org.codeforamerica.shiba.output.Recipient.CASEWORKER;

@Component
public class MnitDocumentConsumer {

    private final MnitEsbWebServiceClient mnitClient;
    private final XmlGenerator xmlGenerator;
    private final PdfGenerator pdfGenerator;
    private final ApplicationDataParser<List<Document>> documentListParser;

    public MnitDocumentConsumer(MnitEsbWebServiceClient mnitClient,
                                XmlGenerator xmlGenerator,
                                PdfGenerator pdfGenerator,
                                ApplicationDataParser<List<Document>> documentListParser) {
        this.mnitClient = mnitClient;
        this.xmlGenerator = xmlGenerator;
        this.pdfGenerator = pdfGenerator;
        this.documentListParser = documentListParser;
    }

    public void process(Application application) {
        Sentry.configureScope(scope -> scope.setContexts("applicationId", application.getId()));

        documentListParser.parse(application.getApplicationData()).forEach(documentType -> mnitClient.send(
                pdfGenerator.generate(application.getId(), documentType, CASEWORKER), application.getCounty())
        );
        mnitClient.send(xmlGenerator.generate(application.getId(), CAF, CASEWORKER), application.getCounty());
    }
}
