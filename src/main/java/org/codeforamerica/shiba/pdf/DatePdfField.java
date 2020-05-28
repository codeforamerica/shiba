package org.codeforamerica.shiba.pdf;

import lombok.Value;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Value
public class DatePdfField implements PdfField {
    String name;
    String value;

    private final static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    public DatePdfField(String name, LocalDate value) {
        this.name = name;
        this.value = value.format(formatter);
    }

}
