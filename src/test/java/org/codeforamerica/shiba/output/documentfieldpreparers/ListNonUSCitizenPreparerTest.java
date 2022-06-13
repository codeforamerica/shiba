package org.codeforamerica.shiba.output.documentfieldpreparers;

import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.output.DocumentFieldType;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.testutilities.TestApplicationDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ListNonUSCitizenPreparerTest {

    ListNonUSCitizenPreparer preparer = new ListNonUSCitizenPreparer();
    TestApplicationDataBuilder applicationDataTest;

    @BeforeEach
    void setup() {
        applicationDataTest = new TestApplicationDataBuilder();
    }

    //Positive

    /**
     * If everyone is selected
     */

    @Test
    void preparesFieldsForApplicantAndLiveInSpouseNamesWhenEveryoneInHouseNotUSCitizen() {
        ApplicationData applicationData = applicationDataTest
                .withPersonalInfo()
                .withHouseholdMember("Other", "Person")
                .withLiveInSpouseAsHouseholdMember()
                .withPageData("usCitizen", "isUsCitizen", "false")
                .withPageData("whoIsNonCitizen", "whoIsNonCitizen", List.of(
                        "Jane Doe applicant",
                        "Daria Agàta someGuid",
                        "Other Person notSpouse"
                ))
                .build();

        List<DocumentField> result = preparer.prepareDocumentFields(Application.builder()
                .applicationData(applicationData)
                .build(), null, Recipient.CASEWORKER);

        assertThat(result).isEqualTo(List.of(
                new DocumentField(
                        "whoIsNonUsCitizen",
                        "nameOfApplicantOrSpouse1",
                        List.of("Jane Doe"),
                        DocumentFieldType.SINGLE_VALUE
                ),
                new DocumentField(
                        "whoIsNonUsCitizen",
                        "nameOfApplicantOrSpouse2",
                        List.of("Daria Agàta"),
                        DocumentFieldType.SINGLE_VALUE
                )));
    }
}
