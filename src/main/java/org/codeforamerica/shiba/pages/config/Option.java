package org.codeforamerica.shiba.pages.config;

import org.codeforamerica.shiba.inputconditions.Condition;
import lombok.Data;

@Data
public class Option {

  private String value;
  private String messageKey;
  private Boolean isNone;
  private String helpMessageKey;
  private String helpIcon;
  private String cssClass;
  private boolean limitSelection = false;
  private String flag;
  private Condition condition;
}
