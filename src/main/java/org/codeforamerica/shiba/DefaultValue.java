package org.codeforamerica.shiba;

import lombok.Data;

@Data
public class DefaultValue {
    DefaultValueType type = DefaultValueType.LITERAL;
    String value;
}
