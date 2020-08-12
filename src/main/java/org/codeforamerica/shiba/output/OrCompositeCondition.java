package org.codeforamerica.shiba.output;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.codeforamerica.shiba.inputconditions.Condition;
import org.codeforamerica.shiba.pages.data.ApplicationData;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class OrCompositeCondition extends CompositeCondition {
    @Override
    public boolean appliesTo(ApplicationData applicationData) {
        return conditions.stream().anyMatch(getConditionPredicate(applicationData));
    }

    public void setConditions(List<Condition> conditions) {
        this.conditions = conditions;
    }
}
