package org.codeforamerica.shiba;

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
    public List<ApplicationInput> map(PagesData data) {
        return pagesWithCustomInputs.stream()
                .flatMap(pageName -> data.getPage(pageName).entrySet().stream()
                        .map(entry -> new ApplicationInput(
                                pageName,
                                entry.getValue().getValue(),
                                entry.getKey(),
                                ApplicationInputType.SINGLE_VALUE)))
                .collect(Collectors.toList());
    }
}
