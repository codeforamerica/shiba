package org.codeforamerica.shiba.output;

import org.codeforamerica.shiba.Application;
import org.codeforamerica.shiba.MnitEsbWebServiceClient;
import org.codeforamerica.shiba.output.applicationinputsmappers.ApplicationInputsMappers;
import org.codeforamerica.shiba.output.pdf.PdfFieldFiller;
import org.codeforamerica.shiba.output.pdf.PdfFieldMapper;
import org.codeforamerica.shiba.output.xml.XmlGenerator;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MnitDocumentConsumer implements ApplicationDataConsumer {

    private final MnitEsbWebServiceClient mnitClient;
    private final XmlGenerator xmlGenerator;
    private final ApplicationInputsMappers mappers;
    private final PdfFieldMapper pdfFieldMapper;
    private final PdfFieldFiller pdfFieldFiller;

    public MnitDocumentConsumer(MnitEsbWebServiceClient mnitClient,
                                XmlGenerator xmlGenerator,
                                ApplicationInputsMappers mappers,
                                PdfFieldMapper pdfFieldMapper,
                                PdfFieldFiller cafWithCoverPageFieldFiller) {
        this.mnitClient = mnitClient;
        this.xmlGenerator = xmlGenerator;
        this.mappers = mappers;
        this.pdfFieldMapper = pdfFieldMapper;
        this.pdfFieldFiller = cafWithCoverPageFieldFiller;
    }

    public void process(Application application) {
        List<ApplicationInput> applicationInputs = mappers.map(application);
        mnitClient.send(pdfFieldFiller.fill(pdfFieldMapper.map(applicationInputs), application.getId()), application.getCounty());
        mnitClient.send(xmlGenerator.generate(applicationInputs, application.getId()), application.getCounty());
    }
}
