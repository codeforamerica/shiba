package org.codeforamerica.shiba.pdf;

import org.codeforamerica.shiba.FormInput;
import org.codeforamerica.shiba.FormInputType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

    public List<PdfField> map(Map<String, List<FormInput>> screens) {
        return screens.entrySet().stream()
                .flatMap(entry -> {
                    String screenName = entry.getKey();
                    return entry.getValue().stream()
                            .filter(input -> input.getValue() != null)
                            .filter(input -> pdfFieldMap.containsKey(String.join(".", screenName, input.getName())))
                            .filter(input -> input.getValue().stream().noneMatch(valueExclusions::contains))
                            .map(input -> {
                                String value = input.getType() == FormInputType.DATE ?
                                        String.join("/", input.getValue()) :
                                        input.getValue().get(0);
                                return new SimplePdfField(
                                        pdfFieldMap.get(String.join(".", screenName, input.getName())),
                                        value);
                            });
                }).collect(Collectors.toList());
    }
}
