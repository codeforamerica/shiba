package org.codeforamerica.shiba.output;

import org.codeforamerica.shiba.pages.data.PagesData;

import java.util.List;

public abstract class CompositeCondition {
    protected List<DerivedValueCondition> conditions;

    abstract boolean appliesTo(PagesData pagesData);
}
