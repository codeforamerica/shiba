package org.codeforamerica.shiba.pdf;

import lombok.Value;

import java.util.Optional;

@Value
public class SimplePdfField implements PdfField {
    String name;
    String value;

    public SimplePdfField(String name, String value) {
        this.name = name;
        this.value = Optional.ofNullable(value).orElse("");
    }

}
