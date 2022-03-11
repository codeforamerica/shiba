package org.codeforamerica.shiba.output.caf;

public enum SnapExpeditedEligibility implements Eligibility{
  ELIGIBLE("SNAP"),
  NOT_ELIGIBLE(""), // leave blank on cover page
  UNDETERMINED(""); // leave blank on cover page

  private final String status;

  SnapExpeditedEligibility(String status) {
    this.status = status;
  }

  public String getStatus() {
    return status;
  }
}
