package org.codeforamerica.shiba;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class PersonalInfo {
    private @NotBlank String firstName;
    private @NotBlank String lastName;
}
