package org.codeforamerica.shiba.pages.config;

import java.util.List;
import java.util.Map;
import lombok.Data;
import org.codeforamerica.shiba.inputconditions.Condition;

@Data
public class PageGroupConfiguration {

  private List<String> completePages;
  private List<String> startPages;
  private String reviewPage;
  private String deleteWarningPage;
  private String redirectPage;
  private String restartPage;
  private Integer startingCount;
  private Map<String, Condition> addedScope;
}
