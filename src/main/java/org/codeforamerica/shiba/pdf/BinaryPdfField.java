package org.codeforamerica.shiba.pdf;

import lombok.Value;

import static org.apache.pdfbox.cos.COSName.Off;
import static org.apache.pdfbox.cos.COSName.YES;

@Value
public class BinaryPdfField implements PdfField {
    String name;
    String value;

    public BinaryPdfField(String name, Boolean value) {
        this.name = name;
        this.value = value ? YES.getName() : Off.getName();
    }
}
