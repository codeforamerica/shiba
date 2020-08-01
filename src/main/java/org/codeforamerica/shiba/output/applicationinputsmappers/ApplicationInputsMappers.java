package org.codeforamerica.shiba.output.applicationinputsmappers;

import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ApplicationInputsMappers {
    private final List<ApplicationInputsMapper> mappers;

    public ApplicationInputsMappers(List<ApplicationInputsMapper> mappers) {
        this.mappers = mappers;
    }

    public List<ApplicationInput> map(ApplicationData data) {
        return this.mappers.stream()
                .flatMap(mapper -> mapper.map(data).stream())
                .collect(Collectors.toList());
    }
}
