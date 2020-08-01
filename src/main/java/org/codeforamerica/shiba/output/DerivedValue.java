package org.codeforamerica.shiba.output;

import lombok.Data;
import org.codeforamerica.shiba.pages.data.ApplicationData;

import java.util.Optional;

@Data
public class DerivedValue {
    private DerivedValueConfiguration value;
    private ApplicationInputType type;
    private CompositeCondition condition;

    public boolean shouldDeriveValue(ApplicationData applicationData){
        return Optional.ofNullable(condition)
                .map(compositeCondition -> compositeCondition.appliesTo(applicationData.getPagesData()))
                .orElse(true);
    }
}
