package org.codeforamerica.shiba.pages.config;

public enum FeatureFlag {
  ON("on"),
  OFF("off");

  protected final String flag;

  FeatureFlag(String flag) {
    this.flag = flag;
  }

  public boolean isOn() {
    return this == ON;
  }

  public boolean isOff() {
    return this == OFF;
  }
}
