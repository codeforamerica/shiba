package org.codeforamerica.shiba.pages;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Data
public class PageWorkflowConfiguration {
    private List<NextPage> nextPages;
    private Condition skipCondition;
    private List<PageDatasource> datasources = new ArrayList<>();
    private boolean conditionalNavigation = false;
    private PageConfiguration pageConfiguration;

    public String getNextPageName(InputDataMap inputDataMap, Integer option) {
        if (!conditionalNavigation) {
            return nextPages.get(option).getPageName();
        }

        return nextPages.stream()
                .filter(nextPage -> Optional.ofNullable(nextPage.condition)
                        .map(condition -> condition.appliesTo(inputDataMap))
                        .orElse(true))
                .findFirst()
                .map(NextPage::getPageName)
                .orElseThrow(() -> new RuntimeException("Cannot find suitable next page."));
    }

    boolean shouldSkip(PagesData pagesData) {
        if (this.skipCondition == null) {
            return false;
        }
        return this.skipCondition.appliesTo(pagesData.getInputDataMapBy(datasources));
    }

    public String resolveTitle(PagesData pagesData) {
        return resolve(pagesData, pageConfiguration.getPageTitle());
    }

    public String resolveHeader(PagesData pagesData) {
        return resolve(pagesData, pageConfiguration.getHeaderKey());
    }

    private String resolve(PagesData pagesData, Value value) {
        if (value == null) {
            return "";
        }
        return value.getConditionalValues().stream()
                .filter(conditionalValue -> {
                    Objects.requireNonNull(this.getDatasources(),
                            "Configuration mismatch! Conditional value cannot be evaluated without a datasource.");
                    InputDataMap inputDataMap = pagesData.getInputDataMapBy(this.datasources);
                    return conditionalValue.getCondition().appliesTo(inputDataMap);
                })
                .findFirst()
                .map(ConditionalValue::getValue)
                .orElse(value.getValue());
    }

}
