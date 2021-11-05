package org.codeforamerica.shiba.output.documentfieldpreparers;

import static java.util.stream.Collectors.toList;
import static org.codeforamerica.shiba.output.DocumentFieldType.SINGLE_VALUE;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.Recipient;
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

    SubworkflowIterationScopeTracker scopeTracker = new SubworkflowIterationScopeTracker();

    Stream<DocumentField> inputs = this.preparers.stream()
        .flatMap(preparer -> {
          try {
            return preparer.prepareDocumentFields(application, document, recipient, scopeTracker)
                .stream();
          } catch (Exception e) {
            log.error("There was an issue preparing application data for " + preparer.getClass()
                .getSimpleName(), e);
            return Stream.empty();
          }
        });

    Stream<DocumentField> defaultInputs = Stream.of(
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
            SINGLE_VALUE));

    return Stream.concat(defaultInputs, inputs).collect(toList());
  }
}
