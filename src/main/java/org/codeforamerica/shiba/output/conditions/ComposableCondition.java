package org.codeforamerica.shiba.output.conditions;

import org.codeforamerica.shiba.pages.PagesData;

import java.util.List;

public abstract class ComposableCondition implements Condition {
    List<Condition> conditions;

    public abstract boolean isTrue(PagesData pagesData);
}
