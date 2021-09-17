package org.codeforamerica.shiba.output.caf;

public enum SnapExpeditedEligibility {
  ELIGIBLE("SNAP"),
  NOT_ELIGIBLE("");

  private final String status;

  SnapExpeditedEligibility(String status) {
    this.status = status;
  }

  public String getStatus() {
    return status;
  }
}
