package org.codeforamerica.shiba;

import lombok.Data;

@Data
public class Validator {
    Validation validation = Validation.NONE;
    ValidationCondition condition;
}
