package org.codeforamerica.shiba.pages.data;

import org.codeforamerica.shiba.inputconditions.Condition;

import java.io.Serial;
import java.util.HashMap;
import java.util.stream.Stream;

public class DatasourcePages extends HashMap<String, PageData> {
    @Serial
    private static final long serialVersionUID = 6366043143114427707L;

    public DatasourcePages(PagesData pagesData) {
        super(pagesData);
    }

    public Boolean satisfies(Condition condition) {
        if (condition.getConditions() != null) {
            Stream<Condition> conditionStream = condition.getConditions().stream();
            return switch (condition.getLogicalOperator()) {
                case AND -> conditionStream.allMatch(this::satisfies);
                case OR -> conditionStream.anyMatch(this::satisfies);
            };
        }

        PageData pageData = this.get(condition.getPageName());
        if (pageData == null || !pageData.containsKey(condition.getInput())) {
            // This page's skipCondition was satisfied, so the client didn't provide an answer, so this condition can't be satisfied
            return false;
        } else {
            return condition.matches(pageData, new PagesData(this));
        }
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
