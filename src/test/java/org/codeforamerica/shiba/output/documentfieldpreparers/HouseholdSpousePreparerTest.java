package org.codeforamerica.shiba.output.documentfieldpreparers;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.output.DocumentFieldType;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.testutilities.TestApplicationDataBuilder;
import org.junit.jupiter.api.Test;

class HouseholdSpousePreparerTest {

  HouseholdSpousePreparer preparer = new HouseholdSpousePreparer(null);
  TestApplicationDataBuilder applicationDataTest = new TestApplicationDataBuilder();

  @Test
  void preparesFieldsForApplicantAndSpouseNameasHouseholdMember() {
    ApplicationData applicationData = applicationDataTest
        .withPersonalInfo()
        .withMultipleHouseholdMembers() // includes spouse
        .build();

    List<DocumentField> result = preparer.prepareDocumentFields(Application.builder()
        .applicationData(applicationData)
        .build(), null, Recipient.CASEWORKER);

    assertThat(result).isEqualTo(List.of(
        new DocumentField(
            "householdMemberWithoutSpouseInfo",
            "firstName",
            List.of("Other"),
            DocumentFieldType.SINGLE_VALUE
            ,0
        ),
        new DocumentField(
            "householdMemberWithoutSpouseInfo",
            "lastName",
            List.of("Person"),
            DocumentFieldType.SINGLE_VALUE
            ,0
        ),
        new DocumentField(
            "householdMemberWithoutSpouseInfo",
            "relationship",
            List.of("housemate"),
            DocumentFieldType.SINGLE_VALUE
            ,0
        ),
        new DocumentField(
            "householdMemberWithoutSpouseInfo",
            "dateOfBirth",
            List.of("5", "6", "1978"),
            DocumentFieldType.DATE_VALUE
            ,0
        ),
        new DocumentField(
            "householdMemberWithoutSpouseInfo",
            "sex",
            List.of("Female"),
            DocumentFieldType.ENUMERATED_SINGLE_VALUE
            ,0
        ),
        new DocumentField(
            "householdMemberWithoutSpouseInfo",
            "maritalStatus",
            List.of("Never married"),
            DocumentFieldType.ENUMERATED_SINGLE_VALUE
            ,0
        ),
        new DocumentField(
            "householdMemberWithoutSpouseInfo",
            "ssnYesNo",
            List.of("Yes"),
            DocumentFieldType.SINGLE_VALUE
            ,0
        ),
        new DocumentField(
            "householdMemberWithoutSpouseInfo",
            "ssn",
            List.of("123121234"),
            DocumentFieldType.SINGLE_VALUE
            ,0
        ),
        //Spouse
        new DocumentField(
            "spouseInfo",
            "firstName",
            List.of("Daria"),
            DocumentFieldType.SINGLE_VALUE
        ),
        new DocumentField(
            "spouseInfo",
            "lastName",
            List.of("Agàta"),
            DocumentFieldType.SINGLE_VALUE
        ),
        new DocumentField(
            "spouseInfo",
            "relationship",
            List.of("spouse"),
            DocumentFieldType.SINGLE_VALUE
        ),
        new DocumentField(
            "spouseInfo",
            "dateOfBirth",
            List.of("5", "6", "1978"),
            DocumentFieldType.DATE_VALUE
        ),
        new DocumentField(
            "spouseInfo",
            "sex",
            List.of("Female"),
            DocumentFieldType.ENUMERATED_SINGLE_VALUE
        ),
        new DocumentField(
            "spouseInfo",
            "maritalStatus",
            List.of("Never married"),
            DocumentFieldType.ENUMERATED_SINGLE_VALUE
        ),
        new DocumentField(
            "spouseInfo",
            "ssnYesNo",
            List.of("Yes"),
            DocumentFieldType.SINGLE_VALUE
        ),
        new DocumentField(
            "spouseInfo",
            "ssn",
            List.of("123121235"),
            DocumentFieldType.SINGLE_VALUE
        )
        ));
  }


}
