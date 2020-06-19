package org.codeforamerica.shiba;

import org.springframework.stereotype.Component;

import java.util.AbstractMap;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.codeforamerica.shiba.ApplicationInputsMapper.formInputTypeToApplicationInputType;

@Component
public class OneToOneApplicationInputsMapper implements ApplicationInputsMapper {
    private final PagesConfiguration pagesConfiguration;

    public OneToOneApplicationInputsMapper(PagesConfiguration pagesConfiguration) {
        this.pagesConfiguration = pagesConfiguration;
    }

    @Override
    public List<ApplicationInput> map(PagesData data) {
        return pagesConfiguration.entrySet().stream()
                .flatMap(entry -> entry.getValue().getFlattenedInputs().stream()
                        .map(input -> new AbstractMap.SimpleEntry<>(entry.getKey(), input)))
                .map(entry -> {
                    FormInput formInput = entry.getValue();
                    InputData inputData = data.getPage(entry.getKey()).get(formInput.getName());
                    return new ApplicationInput(
                            entry.getKey(),
                            inputData.getValue(),
                            formInput.getName(),
                            formInputTypeToApplicationInputType(formInput.getType()));
                })
                .collect(toList());
    }
}
