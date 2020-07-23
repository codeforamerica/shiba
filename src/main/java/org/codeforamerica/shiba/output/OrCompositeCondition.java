package org.codeforamerica.shiba.output;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.codeforamerica.shiba.pages.PagesData;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class OrCompositeCondition extends CompositeCondition {
    @Override
    public boolean appliesTo(PagesData pagesData) {
        return conditions.stream()
                .anyMatch(condition ->
                        condition.appliesTo(pagesData.getPage(condition.getPageName()))
                );
    }

    public void setConditions(List<DerivedValueCondition> conditions) {
        this.conditions = conditions;
    }
}
