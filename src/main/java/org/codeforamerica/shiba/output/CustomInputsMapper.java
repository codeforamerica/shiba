package org.codeforamerica.shiba.output;

import org.codeforamerica.shiba.pages.ApplicationData;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CustomInputsMapper implements ApplicationInputsMapper {
    private final List<String> pagesWithCustomInputs;

    public CustomInputsMapper(List<String> pagesWithCustomInputs) {
        this.pagesWithCustomInputs = pagesWithCustomInputs;
    }

    @Override
    public List<ApplicationInput> map(ApplicationData data) {
        return pagesWithCustomInputs.stream()
                .flatMap(pageName -> data.getPagesData().getPage(pageName).entrySet().stream()
                        .map(entry -> new ApplicationInput(
                                pageName,
                                entry.getValue().getValue(),
                                entry.getKey(),
                                ApplicationInputType.SINGLE_VALUE)))
                .collect(Collectors.toList());
    }
}
