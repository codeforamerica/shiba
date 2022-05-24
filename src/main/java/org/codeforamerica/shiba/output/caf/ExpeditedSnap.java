package org.codeforamerica.shiba.output.caf;

public enum ExpeditedSnap {
  ELIGIBLE("SNAP"),
  NOT_ELIGIBLE("");

  private final String status;

  ExpeditedSnap(String status) {
    this.status = status;
  }

  public String getStatus() {
    return status;
  }
}
