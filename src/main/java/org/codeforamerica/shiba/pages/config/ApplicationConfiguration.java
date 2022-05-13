package org.codeforamerica.shiba.pages.config;

import java.util.List;
import java.util.Map;
import lombok.Data;
import org.codeforamerica.shiba.inputconditions.Condition;

@Data
public class ApplicationConfiguration {

  /**
   * Predefined {@link Condition} objects used for rendering and navigation.
   */
  private List<Condition> conditionDefinitions;

  /**
   * List of all defined pages.
   */
  private List<PageConfiguration> pageDefinitions;

  /**
   * Pages with special functionality.
   */
  private LandmarkPagesConfiguration landmarkPages;

  /**
   * Used for page navigation.
   */
  private Map<String, PageWorkflowConfiguration> workflow;

  /**
   * Used for groups/subworkflows - household, jobs
   */
  private Map<String, PageGroupConfiguration> pageGroups;
}
