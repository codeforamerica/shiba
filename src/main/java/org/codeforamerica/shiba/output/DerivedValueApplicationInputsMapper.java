package org.codeforamerica.shiba.output;

import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class DerivedValueApplicationInputsMapper implements ApplicationInputsMapper {
    private final DerivedValuesConfiguration derivedValuesConfiguration;

    public DerivedValueApplicationInputsMapper(DerivedValuesConfiguration derivedValuesConfiguration) {
        this.derivedValuesConfiguration = derivedValuesConfiguration;
    }

    @Override
    public List<ApplicationInput> map(ApplicationData data) {
        return this.derivedValuesConfiguration.entrySet().stream()
                .flatMap(entry -> {
                    String groupName = entry.getKey();
                    return entry.getValue().entrySet().stream()
                            .map(groupEntry -> groupEntry.getValue().stream()
                                    .filter(derivedValue -> derivedValue.shouldDeriveValue(data))
                                    .findFirst()
                                    .map(derivedValue -> {
                                        List<String> value = derivedValue.getValue().resolve(data);

                                        return new ApplicationInput(
                                                groupName,
                                                groupEntry.getKey(),
                                                value,
                                                derivedValue.getType());
                                    }));
                })
                .flatMap(Optional::stream)
                .collect(Collectors.toList());
    }
}
