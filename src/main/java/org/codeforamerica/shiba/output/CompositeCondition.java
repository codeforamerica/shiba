package org.codeforamerica.shiba.output;

import lombok.Data;
import org.codeforamerica.shiba.pages.PagesData;

import java.util.List;
import java.util.stream.Collectors;

import static org.codeforamerica.shiba.output.LogicalOperator.OR;

@Data
public class CompositeCondition {
    private LogicalOperator logicalOperator = OR;
    private List<DerivedValueCondition> conditions;

    boolean appliesTo(PagesData pagesData) {
        List<Boolean> evaluatedConditions = conditions.stream()
                .map(condition -> condition.appliesTo(pagesData.getPage(condition.getPageName())))
                .collect(Collectors.toList());

        return logicalOperator.apply(evaluatedConditions);
    }
}
