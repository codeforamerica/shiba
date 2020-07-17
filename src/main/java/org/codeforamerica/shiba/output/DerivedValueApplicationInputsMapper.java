package org.codeforamerica.shiba.output;

import org.codeforamerica.shiba.pages.ApplicationData;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
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
                            .filter(groupEntry -> shouldDeriveFrom(groupEntry, data))
                            .map(groupEntry -> {
                                DerivedValue derivedValue = groupEntry.getValue();
                                DerivedValueConfiguration derivedValueConfiguration = derivedValue.getValue();

                                List<String> value = derivedValueConfiguration.resolve(data);

                                return new ApplicationInput(
                                        groupName,
                                        value,
                                        groupEntry.getKey(),
                                        derivedValue.getType());
                            });
                })
                .collect(Collectors.toList());
    }

    @NotNull
    private Boolean shouldDeriveFrom(Map.Entry<String, DerivedValue> groupEntry, ApplicationData data) {
        return Optional.ofNullable(groupEntry.getValue().getCondition())
                .map(compositeCondition -> compositeCondition.appliesTo(data.getPagesData()))
                .orElse(true);
    }
}
