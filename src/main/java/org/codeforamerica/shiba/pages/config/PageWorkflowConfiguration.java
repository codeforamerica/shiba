package org.codeforamerica.shiba.pages.config;

import lombok.Data;
import org.codeforamerica.shiba.inputconditions.Condition;

import java.util.ArrayList;
import java.util.List;

@Data
public class PageWorkflowConfiguration {
    private List<NextPage> nextPages;
    private Condition skipCondition;
    private List<PageDatasource> datasources = new ArrayList<>();
    private PageConfiguration pageConfiguration;

    public Boolean getConditionalNavigation() {
        return nextPages.stream().anyMatch(page -> page.getCondition() != null);
    }
}
