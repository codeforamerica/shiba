package org.codeforamerica.shiba.output.documentfieldpreparers;

import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.testutilities.TestApplicationDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.output.DocumentFieldType.SINGLE_VALUE;

class NoSocialSecurityNumberPreparerTest {
    private final NoSocialSecurityNumberPreparer preparer = new NoSocialSecurityNumberPreparer();
    private final TestApplicationDataBuilder testApplicationDataBuilder = new TestApplicationDataBuilder();
    private Application application;

    @BeforeEach
    void setUp() {
        application = Application.builder().applicationData(testApplicationDataBuilder.base().build()).build();
    }

    @Test
    void shouldReturnEmptyListIfSSNCheckboxAndSSNIsNull() {
        assertThat(preparer.prepareDocumentFields(application, Document.CAF, Recipient.CASEWORKER)).isEqualTo(List.of());
    }

    @Test
    void shouldReturnEmptyListIfSSNCheckboxIsNullAndSSNIsBlank() {
        application.setApplicationData(testApplicationDataBuilder.withPageData("personalInfo", "ssn", List.of("")).build());
        assertThat(preparer.prepareDocumentFields(application, Document.CAF, Recipient.CASEWORKER)).isEqualTo(List.of());
    }

    @Test
    void shouldReturnTrueIfOnlyNoSSNCheckboxIsNull() {
        application.setApplicationData(testApplicationDataBuilder.withPageData("personalInfo", "ssn", List.of("123-45-6789")).build());
        assertThat(preparer.prepareDocumentFields(application, Document.CAF, Recipient.CASEWORKER)).isEqualTo(
                List.of(new DocumentField("personalInfo", "noSSNCheck", "true", SINGLE_VALUE))
        );
    }

    @Test
    void shouldReturnFalseIfOnlyNoSSNCheckboxIsChecked() {
        application.setApplicationData(testApplicationDataBuilder.withPageData("personalInfo", "noSSNCheck", List.of("dont-have-ssn")).build());
        assertThat(preparer.prepareDocumentFields(application, Document.CAF, Recipient.CASEWORKER)).isEqualTo(
            List.of(new DocumentField("personalInfo", "noSSNCheck", "false", SINGLE_VALUE))
        );
    }
}