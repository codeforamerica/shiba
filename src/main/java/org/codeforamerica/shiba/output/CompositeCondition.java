package org.codeforamerica.shiba.output;

import lombok.Data;
import org.codeforamerica.shiba.inputconditions.Condition;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Data
public class CompositeCondition {
    // WARNING: compositeConditions and conditions are mutually exclusive - if compositeConditions is present, conditions will not be checked
    private List<CompositeCondition> compositeConditions;
    private List<Condition> conditions;
    private LogicalOperator logicalOperator = LogicalOperator.AND;

    boolean appliesTo(ApplicationData applicationData) {
        Stream<Condition> conditionStream = addIterationConditionsForSubworkflows(applicationData);
        Predicate<Condition> conditionPredicate = getConditionPredicate(applicationData);
        return switch (logicalOperator) {
            case AND -> conditionStream.allMatch(conditionPredicate);
            case OR -> conditionStream.anyMatch(conditionPredicate);
        };
    }

    @NotNull
    private Predicate<Condition> getConditionPredicate(ApplicationData applicationData) {
        return condition -> {
            PagesData pagesData = Optional.ofNullable(condition.getSubworkflow())
                    .map(subworkflow -> applicationData.getSubworkflows().get(subworkflow))
                    .filter(subworkflow -> subworkflow.size() > condition.getIteration())
                    .map(subworkflow -> subworkflow.get(condition.getIteration()).getPagesData())
                    .orElse(applicationData.getPagesData());
            return Optional.ofNullable(pagesData.getPage(condition.getPageName()))
                    .map(pageData -> condition.matches(pageData, pagesData))
                    .orElse(false);
        };
    }

    private Stream<Condition> addIterationConditionsForSubworkflows(ApplicationData applicationData) {
        return conditions.stream().flatMap(condition -> {
            if (condition.appliesForAllIterations()) {
                Integer subworkflowSize = Optional.ofNullable(applicationData.getSubworkflows().get(condition.getSubworkflow()))
                        .map(ArrayList::size)
                        .orElse(0);
                return IntStream.range(0, subworkflowSize).mapToObj(condition::withIteration);
            } else {
                return Stream.of(condition);
            }
        });
    }
}
