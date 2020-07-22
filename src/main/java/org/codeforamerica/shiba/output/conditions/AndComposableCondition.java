package org.codeforamerica.shiba.output.conditions;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.codeforamerica.shiba.pages.PagesData;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class AndComposableCondition extends ComposableCondition {
    @Override
    public boolean isTrue(PagesData pagesData) {
        return conditions.stream().allMatch(condition -> condition.isTrue(pagesData));
    }

    public void setConditions(List<Condition> conditions) {
        this.conditions = conditions;
    }
}
