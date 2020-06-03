package org.codeforamerica.shiba.pdf;

import org.codeforamerica.shiba.ApplicationFile;
import org.codeforamerica.shiba.FormInput;
import org.codeforamerica.shiba.xml.FileGenerator;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

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
    public ApplicationFile generate(Map<String, List<FormInput>> formInputsMap) {
        List<PdfField> pdfFields = pdfFieldMapper.map(formInputsMap);
        return pdfFiller.fill(pdfFields);
    }
}
