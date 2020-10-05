package org.codeforamerica.shiba.output.pdf;

import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.ApplicationRepository;
import org.codeforamerica.shiba.output.ApplicationFile;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.output.applicationinputsmappers.ApplicationInputsMappers;
import org.codeforamerica.shiba.output.caf.FileNameGenerator;
import org.codeforamerica.shiba.output.xml.FileGenerator;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class PdfGenerator implements FileGenerator {
    private final PdfFieldMapper pdfFieldMapper;
    private final Map<Recipient, PdfFieldFiller> pdfFieldFiller;
    private final ApplicationRepository applicationRepository;
    private final ApplicationInputsMappers mappers;
    private final FileNameGenerator fileNameGenerator;

    public PdfGenerator(PdfFieldMapper pdfFieldMapper,
                        Map<Recipient, PdfFieldFiller> pdfFieldFillers,
                        ApplicationRepository applicationRepository,
                        ApplicationInputsMappers mappers,
                        FileNameGenerator fileNameGenerator
                        ) {
        this.pdfFieldMapper = pdfFieldMapper;
        this.pdfFieldFiller = pdfFieldFillers;
        this.applicationRepository = applicationRepository;
        this.mappers = mappers;
        this.fileNameGenerator = fileNameGenerator;
    }

    @Override
    public ApplicationFile generate(String applicationId, Recipient recipient) {
        Application application = applicationRepository.find(applicationId);
        List<ApplicationInput> applicationInputs = mappers.map(application, recipient);

        return pdfFieldFiller.get(recipient).fill(pdfFieldMapper.map(applicationInputs), applicationId, fileNameGenerator.generateFileName(application));
    }
}
