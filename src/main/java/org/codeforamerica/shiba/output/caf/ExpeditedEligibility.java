package org.codeforamerica.shiba.output.caf;

public enum ExpeditedEligibility {
    ELIGIBLE("Expedited-SNAP"),
    NOT_ELIGIBLE("Non-Expedited-SNAP"),
    UNDETERMINED("Undetermined");

    private final String status;

    ExpeditedEligibility(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
