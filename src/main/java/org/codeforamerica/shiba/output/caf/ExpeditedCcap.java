package org.codeforamerica.shiba.output.caf;

public enum ExpeditedCcap {
  ELIGIBLE("CCAP"),
  NOT_ELIGIBLE("");

  private final String status;

  ExpeditedCcap(String status) {
    this.status = status;
  }

  public String getStatus() {
    return status;
  }
}
