package org.codeforamerica.shiba.pages.config;

import java.util.List;
import java.util.Map;
import lombok.Data;
import org.codeforamerica.shiba.inputconditions.Condition;

@Data
public class ApplicationConfiguration {

  private List<Condition> conditionDefinitions;
  private List<PageConfiguration> pageDefinitions;
  private LandmarkPagesConfiguration landmarkPages;
  private Map<String, PageWorkflowConfiguration> workflow;
  private Map<String, PageGroupConfiguration> pageGroups;

  public PageWorkflowConfiguration getPageWorkflow(String pageName) {
    return this.workflow.get(pageName);
  }

  public Map<String, PageWorkflowConfiguration> getWorkflow() {
    return workflow;
  }

}
