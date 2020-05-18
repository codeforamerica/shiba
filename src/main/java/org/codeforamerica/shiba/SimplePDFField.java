package org.codeforamerica.shiba;

import lombok.Value;

import java.util.Map;
import java.util.Optional;

@Value
public class SimplePDFField implements PDFField {
    String name;
    String value;

    public SimplePDFField(String name, String value) {
        this.name = name;
        this.value = Optional.ofNullable(value).orElse("");
    }

    @Override
    public Map<String, String> getInputBindings() {
        return Map.of(name, value);
    }
}
