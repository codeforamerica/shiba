package org.codeforamerica.shiba.output;

import org.codeforamerica.shiba.MnitEsbWebServiceClient;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.pdf.PdfGenerator;
import org.codeforamerica.shiba.output.xml.XmlGenerator;
import org.springframework.stereotype.Component;

import static org.codeforamerica.shiba.output.Recipient.CASEWORKER;

@Component
public class MnitDocumentConsumer implements ApplicationDataConsumer {

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
        mnitClient.send(pdfGenerator.generate(application.getId(), CASEWORKER), application.getCounty());
        mnitClient.send(xmlGenerator.generate(application.getId(), CASEWORKER), application.getCounty());
    }
}
