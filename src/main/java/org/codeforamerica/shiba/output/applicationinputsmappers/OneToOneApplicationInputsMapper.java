package org.codeforamerica.shiba.output.applicationinputsmappers;

import org.codeforamerica.shiba.Application;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.pages.config.ApplicationConfiguration;
import org.codeforamerica.shiba.pages.config.FormInput;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.InputData;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static org.codeforamerica.shiba.output.applicationinputsmappers.ApplicationInputsMapper.formInputTypeToApplicationInputType;

@Component
public class OneToOneApplicationInputsMapper implements ApplicationInputsMapper {
    private final ApplicationConfiguration applicationConfiguration;
    private final Map<String, String> maskedValuesConfiguration;

    public OneToOneApplicationInputsMapper(ApplicationConfiguration applicationConfiguration,
                                           Map<String, String> maskedValuesConfiguration) {
        this.applicationConfiguration = applicationConfiguration;
        this.maskedValuesConfiguration = maskedValuesConfiguration;
    }

    @Override
    public List<ApplicationInput> map(Application application, Recipient recipient) {
        ApplicationData data = application.getApplicationData();
        return applicationConfiguration.getPageDefinitions().stream()
                .flatMap(pageConfiguration -> pageConfiguration.getFlattenedInputs().stream()
                        .map(input -> Map.entry(pageConfiguration.getName(), input)))
                .filter(entry -> data.getPagesData().getPage(entry.getKey()) != null)
                .map(entry -> {
                    FormInput formInput = entry.getValue();
                    List<String> value;
                    if (Recipient.CLIENT.equals(recipient) && maskedValuesConfiguration.get(formInput.getName()) != null) {
                        value = List.of(maskedValuesConfiguration.get(formInput.getName()));
                    } else {
                        value = data.getPagesData().getPage(entry.getKey()).get(formInput.getName()).getValue();
                    }
                    return new ApplicationInput(
                            entry.getKey(),
                            formInput.getName(),
                            value,
                            formInputTypeToApplicationInputType(formInput.getType()));
                })
                .collect(toList());
    }
}
