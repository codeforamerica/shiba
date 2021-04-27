package org.codeforamerica.shiba.output.applicationinputsmappers;

import org.codeforamerica.shiba.PageDataBuilder;
import org.codeforamerica.shiba.PagesDataBuilder;
import org.codeforamerica.shiba.inputconditions.ValueMatcher;
import org.codeforamerica.shiba.inputconditions.Condition;
import org.codeforamerica.shiba.output.applicationinputsmappers.SubworkflowIterationScopeTracker.IterationScopeInfo;
import org.codeforamerica.shiba.pages.config.PageGroupConfiguration;
import org.codeforamerica.shiba.pages.data.Iteration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SubworkflowIterationScopeTrackerTest {
    private PageGroupConfiguration group1;
    private final PagesDataBuilder pagesDataBuilder = new PagesDataBuilder();

    @BeforeEach
    public void setup() {
        Condition condition1 = new Condition("page1", "input1", "true", ValueMatcher.CONTAINS, null, null);
        String prefix1 = "prefix1";
        Condition condition2 = new Condition("page1", "input1", "false", ValueMatcher.CONTAINS, null, null);
        String prefix2 = "prefix2";
        Map<String, Condition> addedScope = Map.of(prefix1, condition1, prefix2, condition2);
        group1 = new PageGroupConfiguration();
        group1.setAddedScope(addedScope);
    }


    @Test
    public void testDuplicateScopeDoesNotIncreaseIterationIndex() {
        Iteration iteration = new Iteration(pagesDataBuilder.build(List.of(
                new PageDataBuilder("page1", Map.of(
                        "input1", List.of("true"),
                        "input2", List.of("coolString")
                ))
        )));

        SubworkflowIterationScopeTracker scopeTracker = new SubworkflowIterationScopeTracker();
        IterationScopeInfo scopeInfo = scopeTracker.getIterationScopeInfo(group1, iteration);

        assertEquals(scopeInfo.getScope(), "prefix1");
        assertEquals(scopeInfo.getIndex(), 0);

        scopeInfo = scopeTracker.getIterationScopeInfo(group1, iteration);

        assertEquals(scopeInfo.getIndex(), 0);
    }
}