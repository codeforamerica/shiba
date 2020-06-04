package org.codeforamerica.shiba.pdf;

import org.codeforamerica.shiba.ApplicationInput;
import org.springframework.stereotype.Component;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class PdfFieldMapper {
    private final Map<String, String> pdfFieldMap;
    private final Set<String> valueExclusions;

    public PdfFieldMapper(
            Map<String, String> pdfFieldMap,
            Set<String> valueExclusions
    ) {
        this.pdfFieldMap = pdfFieldMap;
        this.valueExclusions = valueExclusions;
    }

    public List<PdfField> map(Map<String, List<ApplicationInput>> screens) {
        return screens.entrySet().stream()
                .flatMap(entry -> entry.getValue().stream()
                        .map(input -> new AbstractMap.SimpleEntry<>(entry.getKey(), input)))
                .filter(entry -> entry.getValue().getValue() != null)
                .filter(entry -> !entry.getValue().getValue().isEmpty())
                .filter(entry -> entry.getValue().getValue().stream().noneMatch(valueExclusions::contains))
                .flatMap(entry -> {
                    String screenName = entry.getKey();
                    ApplicationInput input = entry.getValue();
                    return switch (input.getType()) {
                        case DATE_VALUE -> Stream.of(new SimplePdfField(
                                pdfFieldMap.get(String.join(".", screenName, input.getName())),
                                String.join("/", input.getValue())));
                        case ENUMERATED_MULTI_VALUE -> input.getValue().stream()
                                .map(value -> new BinaryPdfField(pdfFieldMap.get(String.join(".", screenName, input.getName(), value))));
                        default -> Stream.of(new SimplePdfField(
                                pdfFieldMap.get(String.join(".", screenName, input.getName())),
                                input.getValue().get(0)));
                    };
                })
                .filter(pdfField -> pdfField.getName() != null)
                .collect(Collectors.toList());
    }
}
