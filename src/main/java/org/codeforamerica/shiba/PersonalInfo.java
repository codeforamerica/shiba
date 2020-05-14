package org.codeforamerica.shiba;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Getter
@Setter
public class PersonalInfo {
    private @NotBlank String firstName;
    private @NotBlank String lastName;
    private String middleName;
    private String otherName;
    private String birthMonth;
    private String birthDay;
    private String birthYear;
    private @Pattern(regexp = "\\d{9}") String ssn;
    private MaritalStatus maritalStatus;
    private Sex sex;
    private Boolean livedInMNWholeLife;
    private String moveToMNMonth;
    private String moveToMNDay;
    private String moveToMNYear;
    private String moveToMNPreviousCity;
}
