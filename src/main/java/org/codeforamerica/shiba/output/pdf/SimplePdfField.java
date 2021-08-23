package org.codeforamerica.shiba.output.pdf;

import java.util.Optional;
import lombok.Value;

@Value
public class SimplePdfField implements PdfField {

  String name;
  String value;

  public SimplePdfField(String name, String value) {
    this.name = name;
    this.value = Optional.ofNullable(value).orElse("");
  }

}
