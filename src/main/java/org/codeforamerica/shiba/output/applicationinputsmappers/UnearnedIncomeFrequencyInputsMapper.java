package org.codeforamerica.shiba.output.applicationinputsmappers;

import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.ApplicationInputType;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.pages.data.InputData;
import org.codeforamerica.shiba.pages.data.PageData;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.codeforamerica.shiba.output.FullNameFormatter.getListOfSelectedFullNames;

@Component
public class UnearnedIncomeFrequencyInputsMapper implements ApplicationInputsMapper {

    @Override
    public List<ApplicationInput> map(Application application, Recipient recipient, SubworkflowIterationScopeTracker scopeTracker) {
        return application.getApplicationData().getPageData("unearnedIncomeSources").keySet().stream()
                .map(inputData -> new ApplicationInput("unearnedIncomeSources", inputData + "Frequency", List.of("Monthly"), ApplicationInputType.SINGLE_VALUE))
                .collect(Collectors.toList());
    }
}
