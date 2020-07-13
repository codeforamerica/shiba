package org.codeforamerica.shiba.pages;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.codeforamerica.shiba.pages.FormData.getFormDataFrom;

@Data
public class PageWorkflowConfiguration {
    List<String> nextPage;
    String previousPage;
    private Condition skipCondition;
    private List<PageDatasource> datasources = new ArrayList<>();

    String getAdjacentPageName(boolean isBackwards) {
        return getAdjacentPageName(isBackwards, 0);
    }

    public String getAdjacentPageName(boolean isBackwards, Integer option) {
        if (isBackwards) {
            return previousPage;
        }
        return nextPage.get(option);
    }

    boolean shouldSkip(PagesData pagesData) {
        if (this.skipCondition == null) {
            return false;
        }
        return this.skipCondition.appliesTo(getFormDataFrom(datasources, pagesData));
    }

    public String resolve(PagesData pagesData, Value value) {
        if (value == null) {
            return "";
        }
        return value.resolve(condition -> {
            Objects.requireNonNull(this.getDatasources(),
                    "Configuration mismatch! Conditional value cannot be evaluated without a datasource.");
            FormData formData = getFormDataFrom(this.getDatasources(), pagesData);
            return condition.appliesTo(formData);
        });
    }

}
