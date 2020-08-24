package org.codeforamerica.shiba.output.applicationinputsmappers;

import org.codeforamerica.shiba.Application;
import org.codeforamerica.shiba.ApplicationRepository;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.codeforamerica.shiba.output.ApplicationInputType.SINGLE_VALUE;

@Component
public class ApplicationInputsMappers {
    private final ApplicationRepository applicationRepository;
    private final List<ApplicationInputsMapper> mappers;

    public ApplicationInputsMappers(ApplicationRepository applicationRepository,
                                    List<ApplicationInputsMapper> mappers) {
        this.applicationRepository = applicationRepository;
        this.mappers = mappers;
    }

    public List<ApplicationInput> map(String applicationId) {
        Application application = applicationRepository.find(applicationId);
        Stream<ApplicationInput> inputs = this.mappers.stream()
                .flatMap(mapper -> mapper.map(application.getApplicationData()).stream());

        Stream<ApplicationInput> defaultInputs = Stream.of(
                new ApplicationInput("nonPagesData", "applicationId", List.of(applicationId), SINGLE_VALUE),
                new ApplicationInput("nonPagesData", "completedDate", List.of(DateTimeFormatter.ISO_LOCAL_DATE.format(application.getCompletedAt())), SINGLE_VALUE),
                new ApplicationInput("nonPagesData", "completedDateTime", List.of(DateTimeFormatter.ISO_DATE_TIME.format(application.getCompletedAt())), SINGLE_VALUE));

        return Stream.concat(defaultInputs, inputs).collect(toList());
    }
}
