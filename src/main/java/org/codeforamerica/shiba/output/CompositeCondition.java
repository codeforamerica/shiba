package org.codeforamerica.shiba.output;

import org.codeforamerica.shiba.inputconditions.Condition;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public abstract class CompositeCondition {
    protected List<Condition> conditions;

    abstract boolean appliesTo(ApplicationData applicationData);

    @NotNull
    protected Predicate<Condition> getConditionPredicate(ApplicationData applicationData) {
        return condition -> {
            PagesData pagesData = Optional.ofNullable(condition.getSubworkflow())
                    .map(subworkflow -> applicationData.getSubworkflows().get(subworkflow))
                    .filter(subworkflow -> subworkflow.size() > condition.getIteration())
                    .map(subworkflow -> subworkflow.get(condition.getIteration()))
                    .orElse(applicationData.getPagesData());
            return Optional.ofNullable(pagesData.getPage(condition.getPageName()))
                    .map(inputDataMap -> inputDataMap.satisfies(condition))
                    .orElse(false);
        };
    }
}
