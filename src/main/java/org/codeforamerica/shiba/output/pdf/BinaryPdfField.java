package org.codeforamerica.shiba.output.pdf;

import lombok.AllArgsConstructor;
import lombok.Value;

import static org.apache.pdfbox.cos.COSName.YES;

@Value
@AllArgsConstructor
public class BinaryPdfField implements PdfField {
    String name;
    String value;

    public BinaryPdfField(String name) {
        this.name = name;
        this.value = YES.getName();
    }
}
