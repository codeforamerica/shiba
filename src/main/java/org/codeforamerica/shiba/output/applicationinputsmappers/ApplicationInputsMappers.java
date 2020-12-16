package org.codeforamerica.shiba.output.applicationinputsmappers;

import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.Recipient;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.codeforamerica.shiba.output.ApplicationInputType.SINGLE_VALUE;

@Component
public class ApplicationInputsMappers {
    private final List<ApplicationInputsMapper> mappers;

    public ApplicationInputsMappers(List<ApplicationInputsMapper> mappers) {
        this.mappers = mappers;
    }

    public List<ApplicationInput> map(Application application, Recipient recipient) {

        SubworkflowIterationScopeTracker scopeTracker = new SubworkflowIterationScopeTracker();

        Stream<ApplicationInput> inputs = this.mappers.stream()
                .flatMap(mapper -> mapper.map(application, recipient, scopeTracker).stream());

        Stream<ApplicationInput> defaultInputs = Stream.of(
                new ApplicationInput("nonPagesData", "applicationId", List.of(application.getId()), SINGLE_VALUE),
                new ApplicationInput("nonPagesData", "completedDate", List.of(DateTimeFormatter.ISO_LOCAL_DATE.format(application.getCompletedAt().withZoneSameInstant(ZoneId.of("America/Chicago")))), SINGLE_VALUE),
                new ApplicationInput("nonPagesData", "completedDateTime", List.of(DateTimeFormatter.ISO_DATE_TIME.format(application.getCompletedAt())), SINGLE_VALUE),
                new ApplicationInput("nonPagesData", "submissionDateTime", List.of(DateTimeFormatter.ofPattern("MM/dd/yyyy' at 'hh:mm a").format(application.getCompletedAt().withZoneSameInstant(ZoneId.of("America/Chicago")))), SINGLE_VALUE));

        return Stream.concat(defaultInputs, inputs).collect(toList());
    }
}
