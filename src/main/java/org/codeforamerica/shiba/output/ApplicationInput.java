package org.codeforamerica.shiba.output;

import lombok.Value;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

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

    public String getPdfName(Map<String, String> pdfFieldMap) {
        String name = pdfFieldMap.get(String.join(".", this.getGroupName(), this.getName()));
        return getNameWithIteration(name);
    }

    public String getMultiValuePdfName(Map<String, String> pdfFieldMap, String value) {
        String name = pdfFieldMap.get(String.join(".", this.getGroupName(), this.getName(), value));
        return getNameWithIteration(name);
    }

    private String getNameWithIteration(String name) {
        return this.getIteration() != null ? name + "_" + this.getIteration() : name;
    }
}
