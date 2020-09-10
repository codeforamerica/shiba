package org.codeforamerica.shiba.output.pdf;

import org.codeforamerica.shiba.Application;
import org.codeforamerica.shiba.ApplicationRepository;
import org.codeforamerica.shiba.output.ApplicationFile;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.output.applicationinputsmappers.ApplicationInputsMappers;
import org.codeforamerica.shiba.output.xml.FileGenerator;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PdfGenerator implements FileGenerator {
    private final PdfFieldMapper pdfFieldMapper;
    private final PdfFieldFiller pdfFieldFiller;
    private final ApplicationRepository applicationRepository;
    private final ApplicationInputsMappers mappers;

    public PdfGenerator(PdfFieldMapper pdfFieldMapper,
                        PdfFieldFiller pdfFieldFiller,
                        ApplicationRepository applicationRepository,
                        ApplicationInputsMappers mappers
                        ) {
        this.pdfFieldMapper = pdfFieldMapper;
        this.pdfFieldFiller = pdfFieldFiller;
        this.applicationRepository = applicationRepository;
        this.mappers = mappers;
    }

    @Override
    public ApplicationFile generate(String applicationId, Recipient recipient) {
        Application application = applicationRepository.find(applicationId);
        List<ApplicationInput> applicationInputs = mappers.map(application, recipient);

        return pdfFieldFiller.fill(pdfFieldMapper.map(applicationInputs), applicationId, application.getFileName());
    }
}
