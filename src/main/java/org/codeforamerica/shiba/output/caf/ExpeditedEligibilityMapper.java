package org.codeforamerica.shiba.output.caf;

import org.codeforamerica.shiba.Application;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.ApplicationInputType;
import org.codeforamerica.shiba.output.applicationinputsmappers.ApplicationInputsMapper;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ExpeditedEligibilityMapper implements ApplicationInputsMapper {
    private final ExpeditedEligibilityDecider eligibilityDecider;

    public ExpeditedEligibilityMapper(ExpeditedEligibilityDecider eligibilityDecider) {
        this.eligibilityDecider = eligibilityDecider;
    }

    @Override
    public List<ApplicationInput> map(Application application) {
        ApplicationData data = application.getApplicationData();
        return List.of(
                new ApplicationInput(
                        "expeditedEligibility",
                        "expeditedEligibility",
                        List.of(eligibilityDecider.decide(data.getPagesData()).getStatus()),
                        ApplicationInputType.SINGLE_VALUE
                ));
    }
}
