package org.codeforamerica.shiba.output.caf;

import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.ApplicationInputType;
import org.codeforamerica.shiba.output.ApplicationInputsMapper;
import org.codeforamerica.shiba.pages.ApplicationData;
import org.codeforamerica.shiba.pages.InputData;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ExpeditedEligibilityMapper implements ApplicationInputsMapper {
    private final ExpeditedEligibilityDecider eligibilityDecider;

    public ExpeditedEligibilityMapper(ExpeditedEligibilityDecider eligibilityDecider) {
        this.eligibilityDecider = eligibilityDecider;
    }

    @Override
    public List<ApplicationInput> map(ApplicationData data) {
        Map<String, InputData> inputDataMap = data.getPagesData().entrySet().stream()
                .flatMap(entry -> entry.getValue().entrySet().stream()
                        .map(page -> Map.entry(entry.getKey() + "_" + page.getKey(), page.getValue()))
                )
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        boolean decision = eligibilityDecider.decide(inputDataMap);

        return List.of(
                new ApplicationInput(
                        "expeditedEligibility",
                        decision ? List.of("ELIGIBLE") : List.of("NOT_ELIGIBLE"),
                        "expeditedEligibility",
                        ApplicationInputType.ENUMERATED_SINGLE_VALUE
                ));
    }
}
