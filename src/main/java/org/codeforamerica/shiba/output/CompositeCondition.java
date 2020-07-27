package org.codeforamerica.shiba.output;

import org.codeforamerica.shiba.inputconditions.Condition;
import org.codeforamerica.shiba.pages.data.PagesData;

import java.util.List;

public abstract class CompositeCondition {
    protected List<Condition> conditions;

    abstract boolean appliesTo(PagesData pagesData);
}
