package org.codeforamerica.shiba.output;

import lombok.Data;

import java.util.List;

@Data
public class PotentialDerivedValues {
    String groupName;
    String fieldName;
    List<DerivedValue> values;
}
