package org.codeforamerica.shiba.output.applicationinputsmappers;

import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.ApplicationInputType;
import org.codeforamerica.shiba.output.Recipient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class UnearnedIncomeFrequencyInputsMapper implements ApplicationInputsMapper {

    @Override
    public List<ApplicationInput> map(Application application, Recipient recipient, SubworkflowIterationScopeTracker scopeTracker) {
        return List.of("unearnedIncomeSources", "unearnedIncomeSourcesCcap")
                .stream()
                .flatMap(pageName ->
                        Optional.ofNullable(application.getApplicationData().getPageData(pageName))
                        .map(pageData -> pageData
                                .entrySet().stream()
                                .filter(inputData -> !inputData.getValue().getValue().isEmpty())
                                .map(inputData ->
                                        new ApplicationInput(
                                                pageName,
                                                inputData.getKey().replace("Amount", "Frequency"),
                                                List.of("Monthly"),
                                                ApplicationInputType.SINGLE_VALUE)
                                ))
                        .orElse(Stream.of()))
                .collect(Collectors.toList());
    }
}
