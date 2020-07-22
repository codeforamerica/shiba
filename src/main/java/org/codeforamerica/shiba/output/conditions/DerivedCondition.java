package org.codeforamerica.shiba.output.conditions;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.codeforamerica.shiba.pages.PagesData;

@EqualsAndHashCode(callSuper = true)
@Data
public class DerivedCondition extends org.codeforamerica.shiba.pages.Condition implements Condition {
    private String pageName;

    @Override
    public boolean isTrue(PagesData pagesData) {
        return this.appliesTo(pagesData.getPage(pageName));
    }
}
