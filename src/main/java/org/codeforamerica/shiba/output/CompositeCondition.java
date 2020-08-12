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
            if (condition.getSubworkflow() != null) {
                return Optional.ofNullable(applicationData.getSubworkflows().get(condition.getSubworkflow()))
                        .map(subworkflow -> subworkflow.size() > condition.getIteration())
                        .orElse(false);
            } else {
                PagesData pagesData = applicationData.getPagesData();
                return Optional.ofNullable(pagesData.getPage(condition.getPageName()))
                        .map(inputDataMap -> inputDataMap.satisfies(condition))
                        .orElse(false);
            }
        };
    }
}
