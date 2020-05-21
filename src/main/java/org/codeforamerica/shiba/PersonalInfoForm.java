package org.codeforamerica.shiba;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;
import java.util.Optional;
import java.util.function.Function;
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
        personalInfoForm.birthMonth = formatDatePart(personalInfo.getDateOfBirth(), LocalDate::getMonthValue, 2);
        personalInfoForm.birthDay = formatDatePart(personalInfo.getDateOfBirth(), LocalDate::getDayOfMonth, 2);
        personalInfoForm.birthYear = formatDatePart(personalInfo.getDateOfBirth(), LocalDate::getYear, 4);
        personalInfoForm.ssn = personalInfo.getSsn();
        personalInfoForm.maritalStatus = personalInfo.getMaritalStatus();
        personalInfoForm.sex = personalInfo.getSex();
        personalInfoForm.livedInMNWholeLife = personalInfo.getLivedInMNWholeLife();
        personalInfoForm.moveToMNMonth = formatDatePart(personalInfo.getMoveToMNDate(), LocalDate::getMonthValue, 2);
        personalInfoForm.moveToMNDay = formatDatePart(personalInfo.getMoveToMNDate(), LocalDate::getDayOfMonth, 2);
        personalInfoForm.moveToMNYear = formatDatePart(personalInfo.getMoveToMNDate(), LocalDate::getYear, 4);
        personalInfoForm.moveToMNPreviousCity = personalInfo.getMoveToMNPreviousCity();
        return personalInfoForm;
    }

    static String formatDatePart(LocalDate date, Function<LocalDate, Integer> datePartExtractor, int digits) {
        return Optional.ofNullable(date).map(dob -> String.valueOf(datePartExtractor.apply(date))).map(datePart -> StringUtils.leftPad(datePart, digits, "0")).orElse("");
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
