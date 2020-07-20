package org.codeforamerica.shiba.output;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.codeforamerica.shiba.pages.PagesData;

import java.util.List;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = true)
public class OrCompositeCondition extends CompositeCondition {
    @Override
    public boolean appliesTo(PagesData pagesData) {
        List<Boolean> evaluatedConditions = conditions.stream()
                .map(condition -> condition.appliesTo(pagesData.getPage(condition.getPageName())))
                .collect(Collectors.toList());

        return evaluatedConditions.contains(true);
    }

    public void setConditions(List<DerivedValueCondition> conditions) {
        this.conditions = conditions;
    }
}
