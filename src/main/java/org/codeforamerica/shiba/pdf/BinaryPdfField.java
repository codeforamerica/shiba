package org.codeforamerica.shiba.pdf;

import lombok.Value;

import static org.apache.pdfbox.cos.COSName.YES;

@Value
public class BinaryPdfField implements PdfField {
    String name;
    String value;

    public BinaryPdfField(String name) {
        this.name = name;
        this.value = YES.getName();
    }
}
