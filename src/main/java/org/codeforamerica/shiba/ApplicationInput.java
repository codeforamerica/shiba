package org.codeforamerica.shiba;

import lombok.Value;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Value
public class ApplicationInput {
    List<String> value;
    String name;
    ApplicationInputType type;

//    TODO: Move this and the enum transformer somewhere
    public static Map<String, List<ApplicationInput>> create(Screens screens, Map<String, FormData> data) {
        return screens.entrySet().stream()
                .map(entry -> new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue().getFlattenedInputs()))
                .map(entry -> new AbstractMap.SimpleEntry<>(
                        entry.getKey(),
                        entry.getValue().stream()
                                .map(input -> {
                                    InputData inputData = data.get(entry.getKey()).get(input.getName());
                                    return new ApplicationInput(inputData.getNonNullValue(), input.getName(), formInputTypeToApplicationInputType(input.getType()));
                                })
                                .collect(toList())))
                .collect(toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));
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
