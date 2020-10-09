package org.codeforamerica.shiba.pages.data;

import org.codeforamerica.shiba.inputconditions.Condition;
import org.codeforamerica.shiba.output.CompositeCondition;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class DatasourcePages extends HashMap<String, PageData> {
    public DatasourcePages(Map<String, PageData> pages) {
        super(pages);
    }

    public Boolean satisfies(Condition condition) {
        List<String> inputValue = this.get(condition.getPageName()).get(condition.getInput()).getValue();
        return condition.matches(inputValue);
    }

    public Boolean satisfies(CompositeCondition compositeCondition) {
        Stream<Condition> conditionStream = compositeCondition.getConditions().stream();
        return switch (compositeCondition.getLogicalOperator()) {
            case AND -> conditionStream.allMatch(this::satisfies);
            case OR -> conditionStream.anyMatch(this::satisfies);
        };
    }
}
