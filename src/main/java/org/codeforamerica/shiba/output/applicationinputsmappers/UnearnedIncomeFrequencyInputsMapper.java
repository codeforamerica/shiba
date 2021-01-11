package org.codeforamerica.shiba.output.applicationinputsmappers;

import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.ApplicationInputType;
import org.codeforamerica.shiba.output.Recipient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UnearnedIncomeFrequencyInputsMapper implements ApplicationInputsMapper {

    @Override
    public List<ApplicationInput> map(Application application, Recipient recipient, SubworkflowIterationScopeTracker scopeTracker) {
        return application.getApplicationData().getPageData("unearnedIncomeSources").entrySet().stream()
                .filter(inputData -> !inputData.getValue().getValue().isEmpty())
                .map(inputData ->
                        new ApplicationInput(
                                "unearnedIncomeSources",
                                inputData.getKey().replace("Amount", "Frequency"),
                                List.of("Monthly"),
                                ApplicationInputType.SINGLE_VALUE)
                )
                .collect(Collectors.toList());
    }
}
