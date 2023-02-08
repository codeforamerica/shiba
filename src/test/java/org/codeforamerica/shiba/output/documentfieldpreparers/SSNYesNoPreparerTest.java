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

class SSNYesNoPreparerTest {

  SSNYesNoPreparer preparer = new SSNYesNoPreparer();
  TestApplicationDataBuilder applicationDataTest = new TestApplicationDataBuilder();

  @Test
  void preparesFieldsForEveryoneInHouseNotUSCitizen() {
    ApplicationData applicationData = applicationDataTest
        .withPersonalInfo()
        .withMultipleHouseholdMembers()
        .build();

    List<DocumentField> result = preparer.prepareDocumentFields(Application.builder()
        .applicationData(applicationData)
        .build(), null, Recipient.CASEWORKER);

    assertThat(result).isEqualTo(List.of(
        
        new DocumentField(
            "householdMemberInfo",
            "ssnYesNo",
            List.of("Yes"),
            DocumentFieldType.SINGLE_VALUE,
            0
        ),
        new DocumentField(
            "householdMemberInfo",
            "ssnYesNo",
            List.of("Yes"),
            DocumentFieldType.SINGLE_VALUE,
            1
        )));
  }
}
