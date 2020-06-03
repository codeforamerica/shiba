package org.codeforamerica.shiba.pdf;

import org.codeforamerica.shiba.ApplicationFile;
import org.codeforamerica.shiba.FormInput;
import org.codeforamerica.shiba.Screens;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PdfService {

    private final PdfFieldMapper pdfFieldMapper;
    private final PdfFieldFiller pdfFiller;

    public PdfService(PdfFieldMapper pdfFieldMapper,
                      PdfFieldFiller pdfFiller) {
        this.pdfFieldMapper = pdfFieldMapper;
        this.pdfFiller = pdfFiller;
    }

    public ApplicationFile generatePdf(Screens screens) {
        Map<String, List<FormInput>> screensMap = screens.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getFlattenedInputs()));
        return pdfFiller.fill(pdfFieldMapper.map(screensMap));
    }
}
