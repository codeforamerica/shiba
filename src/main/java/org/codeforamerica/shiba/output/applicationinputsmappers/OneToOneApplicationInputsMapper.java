package org.codeforamerica.shiba.output.applicationinputsmappers;

import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.pages.config.FormInput;
import org.codeforamerica.shiba.pages.config.PagesConfiguration;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.InputData;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static org.codeforamerica.shiba.output.applicationinputsmappers.ApplicationInputsMapper.formInputTypeToApplicationInputType;

@Component
public class OneToOneApplicationInputsMapper implements ApplicationInputsMapper {
    private final PagesConfiguration pagesConfiguration;

    public OneToOneApplicationInputsMapper(PagesConfiguration pagesConfiguration) {
        this.pagesConfiguration = pagesConfiguration;
    }

    @Override
    public List<ApplicationInput> map(ApplicationData data) {
        return pagesConfiguration.getPageDefinitions().stream()
                .flatMap(pageConfiguration -> pageConfiguration.getFlattenedInputs().stream()
                        .map(input -> Map.entry(pageConfiguration.getName(), input)))
                .filter(entry -> data.getPagesData().getPage(entry.getKey()) != null)
                .map(entry -> {
                    FormInput formInput = entry.getValue();
                    InputData inputData = data.getPagesData().getPage(entry.getKey()).get(formInput.getName());
                    return new ApplicationInput(
                            entry.getKey(),
                            formInput.getName(),
                            inputData.getValue(),
                            formInputTypeToApplicationInputType(formInput.getType()));
                })
                .collect(toList());
    }
}
