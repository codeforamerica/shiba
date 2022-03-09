package org.codeforamerica.shiba.output.documentfieldpreparers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.output.DocumentFieldType.SINGLE_VALUE;
import static org.codeforamerica.shiba.output.DocumentFieldType.ENUMERATED_SINGLE_VALUE;
import java.util.List;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.testutilities.TestApplicationDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MiddleEasternNorthAfricanWhiteAndOthersPreparerTest {
    private final MiddleEasternNorthAfricanWhiteAndOthersPreparer preparer = new MiddleEasternNorthAfricanWhiteAndOthersPreparer();
    private final TestApplicationDataBuilder testApplicationDataBuilder = new TestApplicationDataBuilder();
    private Application application;

    @BeforeEach
    void setUp() {
        application = Application.builder().applicationData(testApplicationDataBuilder.base().build()).build();
    }

   
    @Test
    void shouldWriteToClientReportedFieldWhenMENAAndOtherSelected() {
      application.setApplicationData(testApplicationDataBuilder
          .withPageData("raceAndEthnicity", "raceAndEthnicity", List.of("MIDDLE_EASTERN_OR_NORTH_AFRICAN","SOME_OTHER_RACE_OR_ETHNICITY"))
          .withPageData("raceAndEthnicity", "otherRaceOrEthnicity", "SomeOtherRaceOrEthnicity")
          .build());
      assertThat(preparer.prepareDocumentFields(application, Document.CAF, Recipient.CASEWORKER)).isEqualTo(
          List.of(new DocumentField("raceAndEthnicity", "CLIENT_REPORTED", "SomeOtherRaceOrEthnicity", SINGLE_VALUE))
      );
    }

    @Test
    void shouldWriteToClientReportedFieldWhenMENASelectedOnly() {
        application.setApplicationData(testApplicationDataBuilder.withPageData("raceAndEthnicity", "raceAndEthnicity", List.of("MIDDLE_EASTERN_OR_NORTH_AFRICAN")).build());
        assertThat(preparer.prepareDocumentFields(application, Document.CAF, Recipient.CASEWORKER)).isEqualTo(
            List.of(new DocumentField("raceAndEthnicity", "CLIENT_REPORTED", "Middle Eastern / N. African", SINGLE_VALUE),
                new DocumentField("raceAndEthnicity", "WHITE", "true", ENUMERATED_SINGLE_VALUE) )
        );
    }
    
    @Test
    void shouldNotWriteToClientReportedFieldWhenMENAAndAsianSelected() {
        application.setApplicationData(testApplicationDataBuilder.withPageData("raceAndEthnicity", "raceAndEthnicity", List.of("ASIAN","MIDDLE_EASTERN_OR_NORTH_AFRICAN")).build());
        assertThat(preparer.prepareDocumentFields(application, Document.CAF, Recipient.CASEWORKER)).isEqualTo(
            List.of()
        );
    }
    
    @Test
    void shouldWriteToClientReportedFieldWhenOthersSelected() {
        application.setApplicationData(testApplicationDataBuilder.withPageData("raceAndEthnicity", "raceAndEthnicity", List.of("ASIAN","SOME_OTHER_RACE_OR_ETHNICITY"))
            .withPageData("raceAndEthnicity", "otherRaceOrEthnicity", "SomeOtherRaceOrEthnicity")
            .build());
        assertThat(preparer.prepareDocumentFields(application, Document.CAF, Recipient.CASEWORKER)).isEqualTo(
            List.of(new DocumentField("raceAndEthnicity", "CLIENT_REPORTED", "SomeOtherRaceOrEthnicity", SINGLE_VALUE))
        );
    }
    
    @Test
    void shouldMarkWhiteFieldWhenWhiteSelected() {
        application.setApplicationData(testApplicationDataBuilder.withPageData("raceAndEthnicity", "raceAndEthnicity", List.of("ASIAN","SOME_OTHER_RACE_OR_ETHNICITY","WHITE"))
            .withPageData("raceAndEthnicity", "otherRaceOrEthnicity", "SomeOtherRaceOrEthnicity")
            .build());
        assertThat(preparer.prepareDocumentFields(application, Document.CAF, Recipient.CASEWORKER)).isEqualTo(
            List.of(new DocumentField("raceAndEthnicity", "WHITE", "true", ENUMERATED_SINGLE_VALUE),
                new DocumentField("raceAndEthnicity", "CLIENT_REPORTED", "SomeOtherRaceOrEthnicity", SINGLE_VALUE)
                )
        );
    }
}