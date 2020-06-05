package org.codeforamerica.shiba.pdf;

import org.codeforamerica.shiba.ApplicationFile;
import org.codeforamerica.shiba.ApplicationInput;
import org.codeforamerica.shiba.xml.FileGenerator;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PdfGenerator implements FileGenerator {

    private final PdfFieldMapper pdfFieldMapper;
    private final PdfFieldFiller pdfFiller;

    public PdfGenerator(PdfFieldMapper pdfFieldMapper,
                        PdfFieldFiller pdfFiller) {
        this.pdfFieldMapper = pdfFieldMapper;
        this.pdfFiller = pdfFiller;
    }

    @Override
    public ApplicationFile generate(List<ApplicationInput> applicationInputs) {
        List<PdfField> pdfFields = pdfFieldMapper.map(applicationInputs);
        return pdfFiller.fill(pdfFields);
    }
}
