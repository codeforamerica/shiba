package org.codeforamerica.shiba.testutilities;

public enum DatePart {
  YEAR(3), MONTH(1), DAY(2);

  private final Integer position;

  DatePart(Integer position) {
    this.position = position;
  }

  public Integer getPosition() {
    return this.position;
  }
}
