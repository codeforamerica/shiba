package org.codeforamerica.shiba;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ApplicationInputs {
    static List<ApplicationInput> from(Screens screens, Map<String, FormData> data) {
        return screens.entrySet().stream()
                .flatMap(entry -> entry.getValue().getFlattenedInputs().stream()
                        .map(input -> new AbstractMap.SimpleEntry<>(entry.getKey(), input)))
                .map(entry -> {
                    FormInput formInput = entry.getValue();
                    InputData inputData = data.get(entry.getKey()).get(formInput.getName());
                    return new ApplicationInput(
                            entry.getKey(),
                            inputData.getValue(),
                            formInput.getName(),
                            formInputTypeToApplicationInputType(formInput.getType()));
                })
                .collect(Collectors.toList());
    }

    private static ApplicationInputType formInputTypeToApplicationInputType(FormInputType type) {
        return switch (type) {
            case CHECKBOX -> ApplicationInputType.ENUMERATED_MULTI_VALUE;
            case RADIO -> ApplicationInputType.ENUMERATED_SINGLE_VALUE;
            case DATE -> ApplicationInputType.DATE_VALUE;
            case TEXT, NUMBER -> ApplicationInputType.SINGLE_VALUE;
        };
    }
}
