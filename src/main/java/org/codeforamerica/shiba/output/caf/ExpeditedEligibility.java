package org.codeforamerica.shiba.output.caf;

public enum ExpeditedEligibility {
    ELIGIBLE("Expedited"),
    NOT_ELIGIBLE("Non-Expedited"),
    UNDETERMINED("Undetermined");

    private final String status;

    ExpeditedEligibility(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
