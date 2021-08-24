package org.codeforamerica.shiba.pages.config;

import lombok.Data;
import org.codeforamerica.shiba.application.FlowType;
import org.codeforamerica.shiba.inputconditions.Condition;

@Data
public class NextPage {

  private String pageName;
  private Condition condition;
  private FlowType flow;
  private String flag;
}
