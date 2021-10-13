package org.codeforamerica.shiba.pages.config;

import lombok.Data;

@Data
public class Option {

  private String value;
  private String messageKey;
  private Boolean isNone;
  private String helpMessageKey;
  private String helpIcon;
  private boolean limitSelection = false;
  private String flag;
}
