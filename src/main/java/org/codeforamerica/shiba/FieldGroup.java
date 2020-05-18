package org.codeforamerica.shiba;

import lombok.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FieldGroup {
//    Collection<Field> fields;

    Map<String, String> fields = new HashMap<>();
}
