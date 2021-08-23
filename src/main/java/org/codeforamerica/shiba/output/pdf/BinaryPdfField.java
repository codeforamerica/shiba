package org.codeforamerica.shiba.output.pdf;

import static org.apache.pdfbox.cos.COSName.YES;

import lombok.AllArgsConstructor;
import lombok.Value;

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
