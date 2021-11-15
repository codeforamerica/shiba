package org.codeforamerica.shiba.pages.config;

import java.util.List;
import lombok.Data;

@Data
public class PageGroupConfiguration {

  private List<String> completePages;
  private List<String> startPages;
  private String reviewPage;
  private String deleteWarningPage;
  private String redirectPage;
  private String restartPage;
  private Integer startingCount;
}
