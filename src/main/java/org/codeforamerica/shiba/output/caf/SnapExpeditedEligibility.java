package org.codeforamerica.shiba.output.caf;

public enum SnapExpeditedEligibility {
    ELIGIBLE("Expedited-SNAP"),
    NOT_ELIGIBLE("Non-Expedited-SNAP"),
    UNDETERMINED("Undetermined");

    private final String status;

    SnapExpeditedEligibility(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
