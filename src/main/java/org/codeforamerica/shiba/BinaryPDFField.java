package org.codeforamerica.shiba;

import lombok.Value;

import java.util.Map;

@Value
public class BinaryPDFField implements PDFField {
    String name;

    public BinaryPDFField(String name) {
        this.name = name;
    }

    @Override
    public Map<String, String> getInputBindings() {
        return Map.of(name, "Yes");
    }
}
