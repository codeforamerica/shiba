package org.codeforamerica.shiba;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class FormInputWithFollowUps extends FormInput {
    String followUpsValue;
    List<FormInput> followUps;
}
