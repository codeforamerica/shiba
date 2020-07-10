package org.codeforamerica.shiba.pages;

import lombok.Data;

import java.util.List;

import static org.codeforamerica.shiba.pages.FormData.getFormDataFrom;

@Data
public class NavigationConfiguration {
    List<String> nextPage;
    String previousPage;
    private Condition skipCondition;

    String getAdjacentPageName(boolean isBackwards) {
        return getAdjacentPageName(isBackwards, 0);
    }

    public String getAdjacentPageName(boolean isBackwards, Integer option) {
        if (isBackwards) {
            return previousPage;
        }
        return nextPage.get(option);
    }

    boolean shouldSkip(PagesData pagesData, List<PageDatasource> datasources) {
        if (this.skipCondition == null) {
            return false;
        }
        return this.skipCondition.appliesTo(getFormDataFrom(datasources, pagesData));
    }
}
