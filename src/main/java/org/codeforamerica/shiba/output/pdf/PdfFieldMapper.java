package org.codeforamerica.shiba.output.pdf;

import org.codeforamerica.shiba.output.ApplicationInput;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PdfFieldMapper {
    private final Map<String, String> pdfFieldMap;

    public PdfFieldMapper(Map<String, String> pdfFieldMap) {
        this.pdfFieldMap = pdfFieldMap;
    }

    public List<PdfField> map(List<ApplicationInput> applicationInputs) {
        return applicationInputs.stream()
                .filter(input -> !input.getValue().isEmpty())
                .flatMap(input -> switch (input.getType()) {
                    case DATE_VALUE -> Stream.of(new SimplePdfField(
                            input.getPdfName(pdfFieldMap),
                            String.join("/", input.getValue())));
                    case ENUMERATED_MULTI_VALUE -> input.getValue().stream()
                            .map(value -> new BinaryPdfField(input.getMultiValuePdfName(pdfFieldMap, value)));
                    default -> Stream.of(new SimplePdfField(
                            input.getPdfName(pdfFieldMap),
                            input.getValue().get(0)));
                })
                .filter(pdfField -> pdfField.getName() != null)
                .collect(Collectors.toList());
    }
}
