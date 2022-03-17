package org.codeforamerica.shiba.application;

public enum Status {
  SENDING("sending"),
  DELIVERED("delivered"),
  DELIVERY_FAILED("delivery_failed"),
  RESUBMISSION_FAILED("resubmission_failed"),
  UNDELIVERABLE("undeliverable");

  private final String displayName;

  Status(String displayName) {
    this.displayName = displayName;
  }

  public static Status valueFor(String displayName) {
    return switch (displayName) {
      case "sending" -> SENDING;
      case "delivered" -> DELIVERED;
      case "delivery_failed" -> DELIVERY_FAILED;
      case "resubmission_failed" -> RESUBMISSION_FAILED;
      case "undeliverable" -> UNDELIVERABLE;
      default -> null;
    };
  }

  public String displayName() {
    return displayName;
  }

  @Override
  public String toString() {
    return displayName;
  }
}