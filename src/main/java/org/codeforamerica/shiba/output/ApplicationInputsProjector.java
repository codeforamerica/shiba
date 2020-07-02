package org.codeforamerica.shiba.output;

import org.codeforamerica.shiba.pages.FormData;
import org.codeforamerica.shiba.pages.PageConfiguration;
import org.codeforamerica.shiba.pages.PagesConfiguration;
import org.codeforamerica.shiba.pages.PagesData;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static org.codeforamerica.shiba.output.ApplicationInputsMapper.formInputTypeToApplicationInputType;

@Component
public class ApplicationInputsProjector implements ApplicationInputsMapper {

    private final PagesConfiguration pagesConfiguration;
    private final InputToOutputProjectionConfiguration inputToOutputProjectionConfiguration;

    public ApplicationInputsProjector(
            PagesConfiguration pagesConfiguration,
            InputToOutputProjectionConfiguration inputToOutputProjectionConfiguration) {
        this.pagesConfiguration = pagesConfiguration;
        this.inputToOutputProjectionConfiguration = inputToOutputProjectionConfiguration;
    }

    @Override
    public List<ApplicationInput> map(PagesData data) {
        return pagesConfiguration.entrySet().stream()
                .flatMap(entry -> {
                    FormData formData = data.getPage(entry.getKey());
                    PageConfiguration pageConfiguration = entry.getValue();
                    return Optional.ofNullable(inputToOutputProjectionConfiguration.get(entry.getKey()))
                            .filter(projectionTarget -> Optional.ofNullable(projectionTarget.getCondition())
                                    .map(condition -> condition.appliesTo(formData))
                                    .orElse(true))
                            .stream()
                            .flatMap(projectionTarget -> pageConfiguration.getInputs().stream()
                                    .filter(formInput -> projectionTarget.getInputs().contains(formInput.getName()))
                                    .map(formInput -> new ApplicationInput(
                                            projectionTarget.getGroupName(),
                                            formData.get(formInput.getName()).getValue(),
                                            formInput.getName(),
                                            formInputTypeToApplicationInputType(formInput.getType())
                                    )));
                })
                .collect(toList());
    }
}
