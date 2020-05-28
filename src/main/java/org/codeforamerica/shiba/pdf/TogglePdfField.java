package org.codeforamerica.shiba.pdf;

import lombok.Value;

import java.util.Optional;

import static org.apache.pdfbox.cos.COSName.Off;

@Value
public class TogglePdfField implements PdfField {
    String name;
    String value;

    public TogglePdfField(String name, Boolean value) {
        this.name = name;
        this.value = Optional.ofNullable(value)
                .map(aBoolean -> aBoolean ? "LEFT" : "RIGHT")
                .orElse(Off.getName());
    }

}
