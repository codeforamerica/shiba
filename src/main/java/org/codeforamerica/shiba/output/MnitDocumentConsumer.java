package org.codeforamerica.shiba.output;

import org.codeforamerica.shiba.Application;
import org.codeforamerica.shiba.MnitEsbWebServiceClient;
import org.codeforamerica.shiba.output.applicationinputsmappers.ApplicationInputsMappers;
import org.codeforamerica.shiba.output.pdf.PdfGenerator;
import org.codeforamerica.shiba.output.xml.XmlGenerator;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.codeforamerica.shiba.output.Recipient.CASEWORKER;

@Component
public class MnitDocumentConsumer implements ApplicationDataConsumer {

    private final MnitEsbWebServiceClient mnitClient;
    private final XmlGenerator xmlGenerator;
    private final ApplicationInputsMappers mappers;
    private final PdfGenerator pdfGenerator;

    public MnitDocumentConsumer(MnitEsbWebServiceClient mnitClient,
                                XmlGenerator xmlGenerator,
                                ApplicationInputsMappers mappers,
                                PdfGenerator pdfGenerator) {
        this.mnitClient = mnitClient;
        this.xmlGenerator = xmlGenerator;
        this.mappers = mappers;
        this.pdfGenerator = pdfGenerator;
    }

    public void process(Application application) {
        List<ApplicationInput> applicationInputs = mappers.map(application, CASEWORKER);
        mnitClient.send(pdfGenerator.generate(applicationInputs, application.getId()), application.getCounty());
        mnitClient.send(xmlGenerator.generate(applicationInputs, application.getId()), application.getCounty());
    }
}
