package org.codeforamerica.shiba.output.documentfieldpreparers;

import static org.codeforamerica.shiba.output.DocumentFieldType.SINGLE_VALUE;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.output.Recipient;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DocumentFieldPreparers {

  private final List<DocumentFieldPreparer> preparers;

  public DocumentFieldPreparers(List<DocumentFieldPreparer> preparers) {
    this.preparers = preparers;
  }

  public List<DocumentField> prepareDocumentFields(Application application, Document document,
      Recipient recipient) {

    // Add default fields
    List<DocumentField> fields = new ArrayList<>(getDefaultFields(application));

    // Run all the preparers
    preparers.forEach(preparer -> {
      try {
        fields.addAll(preparer.prepareDocumentFields(application, document, recipient));
      } catch (Exception e) {
        String preparerClassName = preparer.getClass().getSimpleName();
        log.error("There was an issue preparing application data for " + preparerClassName, e);
      }
    });

    return fields;
  }

  @NotNull
  private List<DocumentField> getDefaultFields(Application application) {
    return List.of(
        new DocumentField("nonPagesData", "applicationId", List.of(application.getId()),
            SINGLE_VALUE),
        new DocumentField("nonPagesData", "completedDate", List.of(
            DateTimeFormatter.ISO_LOCAL_DATE.format(
                application.getCompletedAt().withZoneSameInstant(ZoneId.of("America/Chicago")))),
            SINGLE_VALUE),
        new DocumentField("nonPagesData", "completedDateTime",
            List.of(DateTimeFormatter.ISO_DATE_TIME.format(application.getCompletedAt())),
            SINGLE_VALUE),
        new DocumentField("nonPagesData", "submissionDateTime", List.of(
            DateTimeFormatter.ofPattern("MM/dd/yyyy' at 'hh:mm a").format(
                application.getCompletedAt().withZoneSameInstant(ZoneId.of("America/Chicago")))),
            SINGLE_VALUE)
    );
  }
}
