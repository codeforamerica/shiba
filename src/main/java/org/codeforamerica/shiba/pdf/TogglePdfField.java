package org.codeforamerica.shiba.pdf;

import lombok.Value;
import org.codeforamerica.shiba.pdf.PdfField;

import java.util.Map;
import java.util.Optional;

import static org.apache.pdfbox.cos.COSName.Off;

@Value
public class TogglePdfField implements PdfField {
    String name;
    Boolean value;

    public TogglePdfField(String name, Boolean value) {
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
