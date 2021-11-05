package org.codeforamerica.shiba.output.applicationinputsmappers;

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
public class ApplicationInputsMappers {

  private final List<ApplicationInputsMapper> mappers;

  public ApplicationInputsMappers(List<ApplicationInputsMapper> mappers) {
    this.mappers = mappers;
  }

  public List<DocumentField> map(Application application, Document document,
      Recipient recipient) {

    SubworkflowIterationScopeTracker scopeTracker = new SubworkflowIterationScopeTracker();

    Stream<DocumentField> inputs = this.mappers.stream()
        .flatMap(mapper -> {
          try {
            return mapper.prepareDocumentFields(application, document, recipient, scopeTracker)
                .stream();
          } catch (Exception e) {
            log.error("There was an issue mapping application data for mapper " + mapper.getClass()
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
