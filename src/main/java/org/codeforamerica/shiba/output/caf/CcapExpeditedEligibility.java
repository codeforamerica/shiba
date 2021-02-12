package org.codeforamerica.shiba.output.caf;

public enum CcapExpeditedEligibility {
    ELIGIBLE("Expedited-CCAP"),
    NOT_ELIGIBLE("Non-Expedited-CCAP"),
    UNDETERMINED("Undetermined");

    private final String status;

    CcapExpeditedEligibility(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
