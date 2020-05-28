package org.codeforamerica.shiba.pdf;

import lombok.Value;
import org.codeforamerica.shiba.pdf.PdfField;

import java.util.Map;
import java.util.Optional;

@Value
public class SimplePdfField implements PdfField {
    String name;
    String value;

    public SimplePdfField(String name, String value) {
        this.name = name;
        this.value = Optional.ofNullable(value).orElse("");
    }

    @Override
    public Map<String, String> getInputBindings() {
        return Map.of(name, value);
    }
}
