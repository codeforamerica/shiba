package org.codeforamerica.shiba.pdf;

import org.codeforamerica.shiba.ApplicationInput;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PdfFieldMapper {
    private final Map<String, String> pdfFieldMap;
    private final Set<String> valueExclusions;
    private final Set<String> multiValueInputsToUseTheirValue;

    public PdfFieldMapper(
            Map<String, String> pdfFieldMap,
            Set<String> valueExclusions,
            Set<String> multiValueInputsToUseTheirValue) {
        this.pdfFieldMap = pdfFieldMap;
        this.valueExclusions = valueExclusions;
        this.multiValueInputsToUseTheirValue = multiValueInputsToUseTheirValue;
    }

    public List<PdfField> map(List<ApplicationInput> applicationInputs) {
        return applicationInputs.stream()
                .filter(input -> !input.getValue().isEmpty())
                .filter(input -> input.getValue().stream().noneMatch(valueExclusions::contains))
                .flatMap(input -> {
                    String groupName = input.getGroupName();
                    return switch (input.getType()) {
                        case DATE_VALUE -> Stream.of(new SimplePdfField(
                                pdfFieldMap.get(String.join(".", groupName, input.getName())),
                                String.join("/", input.getValue())));
                        case ENUMERATED_MULTI_VALUE -> input.getValue().stream()
                                .map(value -> {
                                    String pdfFieldName = pdfFieldMap.get(String.join(".", groupName, input.getName(), value));
                                    return this.multiValueInputsToUseTheirValue.contains(input.getName()) ?
                                            new BinaryPdfField(pdfFieldName, value) : new BinaryPdfField(pdfFieldName);
                                });
                        default -> Stream.of(new SimplePdfField(
                                pdfFieldMap.get(String.join(".", groupName, input.getName())),
                                input.getValue().get(0)));
                    };
                })
                .filter(pdfField -> pdfField.getName() != null)
                .collect(Collectors.toList());
    }
}
