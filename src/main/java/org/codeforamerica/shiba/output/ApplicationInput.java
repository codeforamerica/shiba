package org.codeforamerica.shiba.output;

import lombok.Value;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

@Value
public class ApplicationInput {
    String groupName;
    String name;
    @NotNull List<String> value;
    ApplicationInputType type;
    Integer iteration;

    public ApplicationInput(String groupName, String name, @NotNull List<String> value, ApplicationInputType type, Integer iteration) {
        this.groupName = groupName;
        this.name = name;
        this.value = value;
        this.type = type;
        this.iteration = iteration;
    }

    public ApplicationInput(String groupName, String name, @NotNull List<String> value, ApplicationInputType type) {
        this.groupName = groupName;
        this.name = name;
        this.value = value;
        this.type = type;
        this.iteration = null;
    }

    public List<String> getPdfName(Map<String, List<String>> pdfFieldMap) {
        List<String> names = pdfFieldMap.get(String.join(".", this.getGroupName(), this.getName()));
        return this.getNameWithIteration(names);
    }

    public String getMultiValuePdfName(Map<String, List<String>> pdfFieldMap, String value) {
        List<String> names = pdfFieldMap.get(String.join(".", this.getGroupName(), this.getName(), value));
        return getNameWithIteration(names).get(0);
    }

    private List<String> getNameWithIteration(List<String> names) {
        if (names == null) {
            return emptyList();
        }

        return names.stream()
                .map(name -> this.getIteration() != null ? name + "_" + this.getIteration() : name)
                .collect(Collectors.toList());
    }
}
