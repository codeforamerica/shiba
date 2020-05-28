package org.codeforamerica.shiba;

import lombok.Value;

import java.util.Map;

import static org.apache.pdfbox.cos.COSName.Off;
import static org.apache.pdfbox.cos.COSName.YES;

@Value
public class BinaryPdfField implements PdfField {
    String name;
    Boolean value;

    public BinaryPdfField(String name, Boolean value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public Map<String, String> getInputBindings() {
        return Map.of(name, this.value ? YES.getName() : Off.getName());
    }
}
