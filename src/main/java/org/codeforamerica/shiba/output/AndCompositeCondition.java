package org.codeforamerica.shiba.output;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.codeforamerica.shiba.inputconditions.Condition;
import org.codeforamerica.shiba.pages.data.PagesData;

import java.util.List;
import java.util.Optional;

@Data
@EqualsAndHashCode(callSuper = true)
public class AndCompositeCondition extends CompositeCondition {
    @Override
    public boolean appliesTo(PagesData pagesData) {
        return conditions.stream()
                .allMatch(condition ->
                        Optional.ofNullable(pagesData.getPage(condition.getPageName()))
                                .map(inputDataMap -> inputDataMap.satisfies(condition))
                                .orElse(false)
                );
    }

    public void setConditions(List<Condition> conditions) {
        this.conditions = conditions;
    }
}
