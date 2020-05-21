package org.codeforamerica.shiba;

import lombok.Value;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Value
public class DatePDFField implements PDFField {
    String fieldName;
    LocalDate localDate;

    private final static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    public DatePDFField(String fieldName, LocalDate localDate) {
        this.fieldName = fieldName;
        this.localDate = localDate;
    }

    @Override
    public Map<String, String> getInputBindings() {
        return Map.of(fieldName, localDate.format(formatter));
    }
}
