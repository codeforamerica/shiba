package org.codeforamerica.shiba.output.conditions;

import org.codeforamerica.shiba.pages.PagesData;

public interface Condition {
    boolean isTrue(PagesData pagesData);
}
