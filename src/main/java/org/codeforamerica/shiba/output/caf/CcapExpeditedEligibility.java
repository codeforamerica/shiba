package org.codeforamerica.shiba.output.caf;

public enum CcapExpeditedEligibility {
  ELIGIBLE("CCAP"),
  NOT_ELIGIBLE("");

  private final String status;

  CcapExpeditedEligibility(String status) {
    this.status = status;
  }

  public String getStatus() {
    return status;
  }
}
