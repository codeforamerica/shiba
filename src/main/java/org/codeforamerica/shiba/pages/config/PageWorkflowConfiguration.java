package org.codeforamerica.shiba.pages.config;

import lombok.Data;
import org.codeforamerica.shiba.inputconditions.Condition;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.Subworkflow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class PageWorkflowConfiguration {
    private List<NextPage> nextPages;
    private Condition skipCondition;
    private List<PageDatasource> datasources = new ArrayList<>();
    private PageConfiguration pageConfiguration;
    private String groupName;
    private String dataMissingRedirect;

    public Boolean getConditionalNavigation() {
        return nextPages.stream().anyMatch(page -> page.getCondition() != null);
    }

    public Map<String, Subworkflow> getSubworkflows(ApplicationData applicationData) {
        return datasources.stream()
                .filter(datasource -> datasource.getGroupName() != null)
                .map(datasource -> Map.entry(datasource.getGroupName(), applicationData.getSubworkflows().get(datasource.getGroupName())))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public boolean inAGroup() {
        return groupName != null;
    }

    public boolean hasRequiredSubworkflows(ApplicationData applicationData) {
        return datasources.stream()
                .filter(datasource -> datasource.getGroupName() != null)
                .allMatch(datasource -> applicationData.getSubworkflows().get(datasource.getGroupName()) != null);
    }
}
