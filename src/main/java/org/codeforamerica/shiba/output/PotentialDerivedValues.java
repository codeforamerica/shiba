package org.codeforamerica.shiba.output;

import java.util.List;
import lombok.Data;

@Data
public class PotentialDerivedValues {

  String groupName;
  String fieldName;
  List<DerivedValue> values;
  Integer iteration;
}
