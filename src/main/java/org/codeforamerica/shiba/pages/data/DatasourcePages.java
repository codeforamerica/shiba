package org.codeforamerica.shiba.pages.data;

import org.codeforamerica.shiba.inputconditions.Condition;
import org.codeforamerica.shiba.output.CompositeCondition;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class DatasourcePages extends HashMap<String, PageData> {
    public DatasourcePages(PagesData pagesData) {
        super(pagesData);
    }

    public Boolean satisfies(Condition condition) {
        PageData pageData = this.get(condition.getPageName());
        if (pageData == null || !pageData.containsKey(condition.getInput())) {
            // This page's skipCondition was satisfied, so the client didn't provide an answer, so this condition can't be satisfied
            return false;
        } else {
            return condition.matches(pageData, new PagesData(this));
        }
    }

    public Boolean satisfies(CompositeCondition compositeCondition) {
        if (compositeCondition.getCompositeConditions() != null) {
            Stream<CompositeCondition> conditionStream = compositeCondition.getCompositeConditions().stream();
            return switch (compositeCondition.getLogicalOperator()) {
                case AND -> conditionStream.allMatch(this::satisfies);
                case OR -> conditionStream.anyMatch(this::satisfies);
            };
        }

        Stream<Condition> conditionStream = compositeCondition.getConditions().stream();
        return switch (compositeCondition.getLogicalOperator()) {
            case AND -> conditionStream.allMatch(this::satisfies);
            case OR -> conditionStream.anyMatch(this::satisfies);
        };
    }

    public DatasourcePages mergeDatasourcePages(DatasourcePages datasourcePages) {
        datasourcePages.forEach((key, value) -> {
            PageData current = this.get(key);
            if(current != null)
                current.mergeInputDataValues(value);
        });
        return this;
    }
}
