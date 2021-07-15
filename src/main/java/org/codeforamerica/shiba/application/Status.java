package org.codeforamerica.shiba.application;

public enum Status {
    IN_PROGRESS("in_progress"),
    SENDING("sending"),
    DELIVERED("delivered"),
    DELIVERY_FAILED("delivery_failed"),
    RESUBMISSION_FAILED("resubmission_failed");

    private final String displayName;

    Status(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
