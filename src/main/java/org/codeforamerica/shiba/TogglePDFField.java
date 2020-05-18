package org.codeforamerica.shiba;

import lombok.Value;

import java.util.Map;
import java.util.Optional;

import static org.apache.pdfbox.cos.COSName.Off;

@Value
public class TogglePDFField implements PDFField {
    String name;
    Boolean value;

    public TogglePDFField(String name, Boolean value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public Map<String, String> getInputBindings() {
        return Map.of(name, Optional.ofNullable(this.value)
                .map(value -> value ? "LEFT" : "RIGHT")
                .orElse(Off.getName()));
    }
}
