package org.codeforamerica.shiba.output.pdf;

import org.codeforamerica.shiba.output.ApplicationFile;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.xml.FileGenerator;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PdfGenerator implements FileGenerator {

    private final PdfFieldMapper pdfFieldMapper;
    private final PdfFieldFiller pdfFiller;

    public PdfGenerator(
            PdfFieldMapper pdfFieldMapper,
            PdfFieldFiller pdfFiller
    ) {
        this.pdfFieldMapper = pdfFieldMapper;
        this.pdfFiller = pdfFiller;
    }

    @Override
    public ApplicationFile generate(List<ApplicationInput> applicationInputs, String applicationId) {
        List<PdfField> pdfFields = pdfFieldMapper.map(applicationInputs);
        return pdfFiller.fill(pdfFields, applicationId);
    }
}
