package org.codeforamerica.shiba;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;
import java.util.Optional;
import java.util.stream.Stream;

import static java.lang.Integer.parseInt;


@Getter
@Setter
public class PersonalInfoForm {
    private @NotBlank String firstName;
    private @NotBlank String lastName;
    private String middleName;
    private String otherName;
    private String birthMonth;
    private String birthDay;
    private String birthYear;
    private @Pattern(regexp = "\\d{9}|\\d{0}") String ssn;
    private MaritalStatus maritalStatus;
    private Sex sex;
    private Boolean livedInMNWholeLife;
    private String moveToMNMonth;
    private String moveToMNDay;
    private String moveToMNYear;
    private String moveToMNPreviousCity;

    public static PersonalInfoForm fromPersonalInfo(PersonalInfo personalInfo) {
        PersonalInfoForm personalInfoForm = new PersonalInfoForm();
        personalInfoForm.firstName = personalInfo.getFirstName();
        personalInfoForm.lastName = personalInfo.getLastName();
        personalInfoForm.middleName = personalInfo.getMiddleName();
        personalInfoForm.otherName = personalInfo.getOtherName();
        personalInfoForm.birthMonth = Optional.ofNullable(personalInfo.getDateOfBirth()).map(dob -> String.valueOf(dob.getMonth().getValue())).orElse("");
        personalInfoForm.birthDay = Optional.ofNullable(personalInfo.getDateOfBirth()).map(dob -> String.valueOf(dob.getDayOfMonth())).orElse("");
        personalInfoForm.birthYear = Optional.ofNullable(personalInfo.getDateOfBirth()).map(dob -> String.valueOf(dob.getYear())).orElse("");
        personalInfoForm.ssn = personalInfo.getSsn();
        personalInfoForm.maritalStatus = personalInfo.getMaritalStatus();
        personalInfoForm.sex = personalInfo.getSex();
        personalInfoForm.livedInMNWholeLife = personalInfo.getLivedInMNWholeLife();
        personalInfoForm.moveToMNMonth = Optional.ofNullable(personalInfo.getMoveToMNDate()).map(moveToMNDate -> String.valueOf(moveToMNDate.getMonthValue())).orElse("");
        personalInfoForm.moveToMNDay = Optional.ofNullable(personalInfo.getMoveToMNDate()).map(moveToMNDate -> String.valueOf(moveToMNDate.getDayOfMonth())).orElse("");
        personalInfoForm.moveToMNYear = Optional.ofNullable(personalInfo.getMoveToMNDate()).map(moveToMNDate -> String.valueOf(moveToMNDate.getYear())).orElse("");
        personalInfoForm.moveToMNPreviousCity = personalInfo.getMoveToMNPreviousCity();
        return personalInfoForm;
    }

    public PersonalInfo mapToPersonalInfo() {
        return new PersonalInfo(
                firstName,
                lastName,
                middleName,
                otherName,
                ssn,
                maritalStatus,
                sex,
                Stream.of(birthYear, birthMonth, birthDay).noneMatch(String::isEmpty) ?
                        LocalDate.of(parseInt(birthYear), parseInt(birthMonth), parseInt(birthDay)) : null,
                livedInMNWholeLife,
                moveToMNPreviousCity,
                Stream.of(moveToMNYear, moveToMNMonth, moveToMNDay).noneMatch(String::isEmpty) ?
                        LocalDate.of(parseInt(moveToMNYear), parseInt(moveToMNMonth), parseInt(moveToMNDay)) : null
        );
    }
}
