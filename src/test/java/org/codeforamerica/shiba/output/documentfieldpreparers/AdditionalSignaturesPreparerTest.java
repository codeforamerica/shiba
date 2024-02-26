package org.codeforamerica.shiba.output.documentfieldpreparers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.output.DocumentFieldType.SINGLE_VALUE;

import java.time.ZonedDateTime;
import java.util.List;

import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.testutilities.TestApplicationDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AdditionalSignaturesPreparerTest {
    private final AdditionalSignaturesPreparer preparer = new AdditionalSignaturesPreparer();
    private final TestApplicationDataBuilder testApplicationDataBuilder = new TestApplicationDataBuilder();
    private Application application;

    @BeforeEach
    void setUp() {
        application = Application.builder().applicationData(testApplicationDataBuilder.base().build()).build();
    }

   
    @Test
    void shouldShowDateWhenSecondSignatureExists() {
      application.setApplicationData(testApplicationDataBuilder
          .withPageData("secondSignature", "secondSignature", "Test Signature 2")
          .build());
      
      application.setCompletedAt(ZonedDateTime.parse("2024-01-05T09:20:16.044874300-06:00[America/Chicago]"));
      
      assertThat(preparer.prepareDocumentFields(application, Document.CAF, Recipient.CLIENT)).isEqualTo(
          List.of(new DocumentField("secondSignature", "createdDateSignature", "2024-01-05", SINGLE_VALUE))
      );
    }
    
    @Test
    void shouldNotShowDateWhenSecondSignatureDoesNotExist() {
      application.setApplicationData(testApplicationDataBuilder
		  .withPageData("signThisApplication", "applicantSignature", "Test Signature 1")
          .build());
      
      application.setCompletedAt(ZonedDateTime.parse("2024-01-05T09:20:16.044874300-06:00[America/Chicago]"));
      
      assertThat(preparer.prepareDocumentFields(application, Document.CAF, Recipient.CLIENT)).doesNotContainSequence(
    		  List.of(new DocumentField("signThisApplicationOtherAdult", "createdDateSignature", "2024-01-05", SINGLE_VALUE)));
    }
}