package org.codeforamerica.shiba.output.pdf;

import org.codeforamerica.shiba.output.ApplicationInput;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PdfFieldMapper {
    private final Map<String, List<String>> pdfFieldMap;
    private final Map<String, String> enumMap;

    public PdfFieldMapper(Map<String, List<String>> pdfFieldMap, Map<String, String> enumMap) {
        this.pdfFieldMap = pdfFieldMap;
        this.enumMap = enumMap;
    }

    public List<PdfField> map(List<ApplicationInput> applicationInputs) {
        return applicationInputs.stream()
                .filter(input -> !input.getValue().isEmpty())
                .flatMap(input -> switch (input.getType()) {
                    case DATE_VALUE -> input.getPdfName(pdfFieldMap).stream().map(
                            pdfName -> new SimplePdfField(pdfName, String.join("/", input.getValue())));
                    case ENUMERATED_MULTI_VALUE -> input.getValue().stream()
                            .map(value -> new BinaryPdfField(input.getMultiValuePdfName(pdfFieldMap, value)));
                    default -> input.getPdfName(pdfFieldMap).stream().map(pdfName ->
                            new SimplePdfField(pdfName, enumMap.getOrDefault(input.getValue().get(0), input.getValue().get(0))));
                })
                .filter(pdfField -> pdfField.getName() != null)
                .collect(Collectors.toList());
    }
}
