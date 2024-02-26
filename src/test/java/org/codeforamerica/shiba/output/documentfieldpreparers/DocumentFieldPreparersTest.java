package org.codeforamerica.shiba.output.documentfieldpreparers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.output.DocumentFieldType.SINGLE_VALUE;
import static org.codeforamerica.shiba.output.Recipient.CASEWORKER;
import static org.codeforamerica.shiba.output.Recipient.CLIENT;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DocumentFieldPreparersTest {

  private DocumentFieldPreparers preparers;

  @BeforeEach
  void setUp() {
    preparers = new DocumentFieldPreparers(List.of());
  }

  @Test
  void shouldIncludeApplicationIdInput() {
    List<DocumentField> documentFields = preparers.prepareDocumentFields(Application.builder()
        .id("someId")
        .completedAt(ZonedDateTime.now())
        .applicationData(new ApplicationData())
        .county(County.Olmsted)
        .timeToComplete(null)
        .build(), null, CLIENT);

    assertThat(documentFields).contains(
        new DocumentField("nonPagesData", "applicationId", List.of("someId"), SINGLE_VALUE));
  }

  @Test
  void shouldIncludeCompletedDateInput() {
    String applicationId = "someId";
    ZonedDateTime completedAt = ZonedDateTime.of(
        LocalDateTime.of(2020, 9, 3, 1, 2, 3),
        ZoneOffset.UTC);
    Application application = Application.builder()
        .id(applicationId)
        .completedAt(completedAt)
        .applicationData(new ApplicationData())
        .county(null)
        .timeToComplete(null)
        .build();

    List<DocumentField> documentFields = preparers.prepareDocumentFields(application, null,
        CASEWORKER);

    assertThat(documentFields).contains(
        new DocumentField("nonPagesData", "completedDate", List.of("2020-09-02"), SINGLE_VALUE));
  }

  @Test
  void shouldIncludeCompletedDateTimeInput() {
    String applicationId = "someId";
    ZonedDateTime completedAt = ZonedDateTime
        .of(LocalDateTime.of(2019, 11, 16, 5, 29, 1), ZoneOffset.UTC);
    Application application = Application.builder()
        .id(applicationId)
        .completedAt(completedAt)
        .applicationData(new ApplicationData())
        .county(null)
        .timeToComplete(null)
        .build();

    List<DocumentField> documentFields = preparers.prepareDocumentFields(application, null,
        CASEWORKER);

    assertThat(documentFields).contains(
        new DocumentField("nonPagesData", "completedDateTime", List.of("2019-11-16T05:29:01Z"),
            SINGLE_VALUE));
  }

  @Test
  void shouldIncludeSubmissionDateTimeInput() {
    String applicationId = "someId";
    ZonedDateTime completedAt = ZonedDateTime.of(
        LocalDateTime.of(2020, 9, 3, 1, 2, 3),
        ZoneId.of("America/Chicago"));
    Application application = Application.builder()
        .id(applicationId)
        .completedAt(completedAt)
        .applicationData(new ApplicationData())
        .county(null)
        .timeToComplete(null)
        .build();

    List<DocumentField> documentFields = preparers.prepareDocumentFields(application, null,
        CASEWORKER);

    assertThat(documentFields).contains(
        new DocumentField("nonPagesData", "submissionDateTime",
            List.of("09/03/2020 at 01:02 AM"), SINGLE_VALUE));
  }

  @Test
  void shouldUseMatchingRecipientAndDocumentForPreparers() {
    DocumentFieldPreparer preparer = mock(DocumentFieldPreparer.class);

    DocumentFieldPreparers documentFieldPreparers = new DocumentFieldPreparers(
        List.of(preparer));

    Application application = Application.builder()
        .id("someId")
        .completedAt(ZonedDateTime.now())
        .applicationData(new ApplicationData())
        .county(County.Olmsted)
        .timeToComplete(null)
        .build();
    documentFieldPreparers.prepareDocumentFields(application, Document.CAF, CASEWORKER);

    verify(preparer).prepareDocumentFields(eq(application), eq(Document.CAF), eq(CASEWORKER)
    );
  }
  
  @Test
  void shouldUseDefaultZonedDateTimeIfCompletedAtIsNull() {
    String applicationId = "someId";
    
    Application application = Application.builder()
        .id(applicationId)
        .completedAt(null)
        .applicationData(new ApplicationData())
        .county(null)
        .timeToComplete(null)
        .build();

    List<DocumentField> documentFields = preparers.prepareDocumentFields(application, null,
        CASEWORKER);

    assertThat(documentFields).contains(
        new DocumentField("nonPagesData", "submissionDateTime",
            List.of("01/01/0001 at 01:01 AM"), SINGLE_VALUE));
  }

  @Test
  void shouldStillSuccessfullyMapEvenWithExceptionsInIndividualPreparers() {
    DocumentFieldPreparer successfulPreparer = mock(DocumentFieldPreparer.class);
    DocumentFieldPreparer failingPreparer = mock(DocumentFieldPreparer.class);
    DocumentFieldPreparers documentFieldPreparers = new DocumentFieldPreparers(
        List.of(failingPreparer, successfulPreparer));
    Application application = Application.builder()
        .id("someId")
        .completedAt(ZonedDateTime.now())
        .applicationData(new ApplicationData())
        .county(County.Olmsted)
        .timeToComplete(null)
        .build();

    List<DocumentField> mockOutput = List
        .of(new DocumentField("group", "name", List.of("value"), null));
    when(successfulPreparer.prepareDocumentFields(eq(application), eq(Document.CAF), eq(CASEWORKER)
    ))
        .thenReturn(mockOutput);
    when(failingPreparer.prepareDocumentFields(eq(application), eq(Document.CAF), eq(CASEWORKER)
    ))
        .thenThrow(IllegalArgumentException.class);

    List<DocumentField> actualOutput = documentFieldPreparers
        .prepareDocumentFields(application, Document.CAF, CASEWORKER);
    assertThat(actualOutput).isNotEmpty();
    verify(successfulPreparer).prepareDocumentFields(eq(application), eq(Document.CAF),
        eq(CASEWORKER));
    verify(failingPreparer).prepareDocumentFields(eq(application), eq(Document.CAF), eq(CASEWORKER)
    );
  }
}
