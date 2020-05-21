package org.codeforamerica.shiba;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;

@Value
@Builder
public class PersonalInfo {
    String firstName;
    String lastName;
    String middleName;
    String otherName;
    String ssn;
    MaritalStatus maritalStatus;
    Sex sex;
    LocalDate dateOfBirth;
    Boolean livedInMNWholeLife;
    String moveToMNPreviousCity;
    LocalDate moveToMNDate;
}
