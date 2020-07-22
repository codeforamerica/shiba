package org.codeforamerica.shiba.output;

import lombok.Data;
import org.codeforamerica.shiba.output.conditions.Condition;
import org.codeforamerica.shiba.pages.ApplicationData;

import java.util.Optional;

@Data
public class DerivedValue {
    private DerivedValueConfiguration value;
    private ApplicationInputType type;
    private Condition condition;

    boolean shouldDeriveValue(ApplicationData applicationData){
        return Optional.ofNullable(condition)
                .map(compositeCondition -> condition.isTrue(applicationData.getPagesData()))
                .orElse(true);
    }
}
