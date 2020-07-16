package org.codeforamerica.shiba.pages;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.codeforamerica.shiba.pages.FormData.getFormDataFrom;

@Data
public class PageWorkflowConfiguration {
    private List<NextPage> nextPages;
    private Condition skipCondition;
    private List<PageDatasource> datasources = new ArrayList<>();
    boolean conditionalNavigation = false;

    public String getNextPageName(FormData formData, Integer option) {
        if (!conditionalNavigation) {
            return nextPages.get(option).getPageName();
        }

        return nextPages.stream()
                .filter(nextPage -> Optional.ofNullable(nextPage.condition)
                        .map(condition -> condition.appliesTo(formData))
                        .orElse(true))
                .findFirst()
                .map(NextPage::getPageName)
                .orElseThrow(() -> new RuntimeException("Cannot find suitable next page."));
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
