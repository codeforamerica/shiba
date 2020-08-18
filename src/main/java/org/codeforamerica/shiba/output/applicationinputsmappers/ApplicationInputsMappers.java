package org.codeforamerica.shiba.output.applicationinputsmappers;

import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ApplicationInputsMappers {
    private final List<ApplicationInputsMapper> mappers;
    private final OutputMappingConfiguration outputMappingConfiguration;

    public ApplicationInputsMappers(
            List<ApplicationInputsMapper> mappers,
            OutputMappingConfiguration outputMappingConfiguration
    ) {
        this.mappers = mappers;
        this.outputMappingConfiguration = outputMappingConfiguration;
    }

    public List<ApplicationInput> map(ApplicationData data) {
        return this.mappers.stream()
                .flatMap(mapper -> mapper.map(data).stream())
                .map(applicationInput -> Optional.ofNullable(outputMappingConfiguration.get(applicationInput.getGroupName()))
                        .map(inputMap -> Optional.ofNullable(inputMap.get(applicationInput.getName()))
                                .map(outputMapping -> new ApplicationInput(
                                        applicationInput.getGroupName(),
                                        applicationInput.getName(),
                                        List.of(outputMapping.get(applicationInput.getValue().get(0))),
                                        applicationInput.getType(),
                                        applicationInput.getIteration()
                                ))
//                                .orElse(applicationInput)
                        )
                        .orElse(Optional.of(applicationInput))
                )
                .flatMap(Optional::stream)
                .collect(Collectors.toList());
    }
}
