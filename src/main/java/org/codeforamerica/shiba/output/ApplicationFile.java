package org.codeforamerica.shiba.output;

import lombok.ToString;
import lombok.Value;

@Value
@ToString(exclude = {"fileBytes"})
public class ApplicationFile {

  byte[] fileBytes;
  String fileName;
}
