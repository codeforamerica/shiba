package org.codeforamerica.shiba.output.pdf;

import org.codeforamerica.shiba.output.ApplicationFile;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.xml.FileGenerator;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PdfGenerator implements FileGenerator {
    private final PdfFieldMapper pdfFieldMapper;
    private final PdfFieldFiller pdfFieldFiller;

    public PdfGenerator(PdfFieldMapper pdfFieldMapper,
                        PdfFieldFiller pdfFieldFiller) {
        this.pdfFieldMapper = pdfFieldMapper;
        this.pdfFieldFiller = pdfFieldFiller;
    }

    @Override
    public ApplicationFile generate(List<ApplicationInput> applicationInputs, String applicationId) {
        return pdfFieldFiller.fill(pdfFieldMapper.map(applicationInputs), applicationId);
    }
}
