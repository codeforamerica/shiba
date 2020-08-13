package org.codeforamerica.shiba.output;

import org.codeforamerica.shiba.MnitEsbWebServiceClient;
import org.codeforamerica.shiba.output.applicationinputsmappers.ApplicationInputsMappers;
import org.codeforamerica.shiba.output.pdf.PdfGenerator;
import org.codeforamerica.shiba.output.xml.XmlGenerator;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.List;

@Component
public class MnitDocumentConsumer implements ApplicationDataConsumer {

    private final MnitEsbWebServiceClient mnitClient;
    private final Clock clock;
    private final PdfGenerator pdfGenerator;
    private final XmlGenerator xmlGenerator;
    private final ApplicationInputsMappers mappers;

    public MnitDocumentConsumer(MnitEsbWebServiceClient mnitClient,
                                Clock clock,
                                PdfGenerator pdfGenerator,
                                XmlGenerator xmlGenerator,
                                ApplicationInputsMappers mappers) {
        this.mnitClient = mnitClient;
        this.clock = clock;
        this.pdfGenerator = pdfGenerator;
        this.xmlGenerator = xmlGenerator;
        this.mappers = mappers;
    }

    public ZonedDateTime process(ApplicationData applicationData) {
        List<ApplicationInput> applicationInputs = mappers.map(applicationData);
        mnitClient.send(pdfGenerator.generate(applicationInputs));
        mnitClient.send(xmlGenerator.generate(applicationInputs));
        return ZonedDateTime.now(clock);
    }
}
