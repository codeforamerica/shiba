package org.codeforamerica.shiba.output;

import org.codeforamerica.shiba.pages.ApplicationData;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class DerivedValueApplicationInputsMapper implements ApplicationInputsMapper {
    private final DerivedValueConfiguration derivedValueConfiguration;

    public DerivedValueApplicationInputsMapper(DerivedValueConfiguration derivedValueConfiguration) {
        this.derivedValueConfiguration = derivedValueConfiguration;
    }

    @Override
    public List<ApplicationInput> map(ApplicationData data) {
        return this.derivedValueConfiguration.entrySet().stream()
                .flatMap(entry -> {
                    String groupName = entry.getKey();
                    return entry.getValue().entrySet().stream()
                            .filter(groupEntry -> Optional.ofNullable(groupEntry.getValue().getCondition())
                                    .map(compositeCondition -> compositeCondition.appliesTo(data.getPagesData()))
                                    .orElse(true)
                            )
                            .map(groupEntry -> new ApplicationInput(
                                    groupName,
                                    List.of(groupEntry.getValue().getValue()),
                                    groupEntry.getKey(),
                                    groupEntry.getValue().getType()));
                })
                .collect(Collectors.toList());
    }
}
