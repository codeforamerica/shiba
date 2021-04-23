package org.codeforamerica.shiba.pages.config;

import lombok.Data;
import org.codeforamerica.shiba.output.Condition;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.Subworkflows;

import java.util.ArrayList;
import java.util.List;

@Data
public class PageWorkflowConfiguration {
    private List<NextPage> nextPages;
    private Condition skipCondition;
    private List<PageDatasource> datasources = new ArrayList<>();
    private PageConfiguration pageConfiguration;
    private String groupName;
    private String appliesToGroup;
    private String dataMissingRedirect;
    private String enrichment;
    private String subtleLinkTargetPage;

    public Boolean isDirectNavigation() {
        return nextPages.stream().noneMatch(page -> page.getCondition() != null || page.getFlag() != null);
    }

    public Subworkflows getSubworkflows(ApplicationData applicationData) {
        return applicationData.getSubworkflowsForPageDatasources(datasources);
    }

    public boolean isInAGroup() {
        return groupName != null;
    }
}
