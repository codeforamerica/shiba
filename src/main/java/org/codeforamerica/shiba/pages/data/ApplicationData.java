package org.codeforamerica.shiba.pages.data;

import lombok.Data;
import org.codeforamerica.shiba.output.caf.PageInputCoordinates;
import org.codeforamerica.shiba.pages.config.NextPage;
import org.codeforamerica.shiba.pages.config.PageWorkflowConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Data
public class ApplicationData {
    private PagesData pagesData = new PagesData();
    private Subworkflows subworkflows = new Subworkflows();
    private Map<String, PagesData> incompleteIterations = new HashMap<>();

    public PageData getInputDataMap(String pageName) {
        return this.pagesData.getPage(pageName);
    }

    public String getValue(PageInputCoordinates pageInputCoordinates) {
        return Optional.ofNullable(this.getInputDataMap(pageInputCoordinates.getPageName()))
                .map(pageData -> pageData.get(pageInputCoordinates.getInputName()).getValue().get(0))
                .orElse(pageInputCoordinates.getDefaultValue());
    }

    public String getNextPageName(PageWorkflowConfiguration pageWorkflowConfiguration, Integer option) {
        if (!pageWorkflowConfiguration.getConditionalNavigation()) {
            return pageWorkflowConfiguration.getNextPages().get(option).getPageName();
        }
        PageData pageData;
        if (pageWorkflowConfiguration.inAGroup()) {
            pageData = incompleteIterations.get(pageWorkflowConfiguration.getGroupName()).get(pageWorkflowConfiguration.getPageConfiguration().getName());
        } else {
            pageData = pagesData.getPage(pageWorkflowConfiguration.getPageConfiguration().getName());
        }

        if (pageData == null) {
            throw new RuntimeException(String.format("Conditional navigation for %s requires page to have data/inputs.", pageWorkflowConfiguration.getPageConfiguration().getName()));
        }

        return pageWorkflowConfiguration.getNextPages().stream()
                .filter(nextPage -> Optional.ofNullable(nextPage.getCondition())
                        .map(pageData::satisfies)
                        .orElse(true))
                .findFirst()
                .map(NextPage::getPageName)
                .orElseThrow(() -> new RuntimeException("Cannot find suitable next page."));
    }

}
