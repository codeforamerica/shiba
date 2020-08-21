package org.codeforamerica.shiba.output;

import org.codeforamerica.shiba.MnitEsbWebServiceClient;
import org.codeforamerica.shiba.output.applicationinputsmappers.ApplicationInputsMappers;
import org.codeforamerica.shiba.output.pdf.PdfGenerator;
import org.codeforamerica.shiba.output.xml.XmlGenerator;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MnitDocumentConsumer implements ApplicationDataConsumer {

    private final MnitEsbWebServiceClient mnitClient;
    private final PdfGenerator pdfGenerator;
    private final XmlGenerator xmlGenerator;
    private final ApplicationInputsMappers mappers;

    public MnitDocumentConsumer(MnitEsbWebServiceClient mnitClient,
                                PdfGenerator pdfGenerator,
                                XmlGenerator xmlGenerator,
                                ApplicationInputsMappers mappers) {
        this.mnitClient = mnitClient;
        this.pdfGenerator = pdfGenerator;
        this.xmlGenerator = xmlGenerator;
        this.mappers = mappers;
    }

    public void process(String applicationId) {
        List<ApplicationInput> applicationInputs = mappers.map(applicationId);
        mnitClient.send(pdfGenerator.generate(applicationInputs));
        mnitClient.send(xmlGenerator.generate(applicationInputs));
    }
}
