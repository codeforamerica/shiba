package org.codeforamerica.shiba.output.caf;

public enum CcapExpeditedEligibility {
    ELIGIBLE("CCAP"),
    NOT_ELIGIBLE(""), // leave blank on cover page
    UNDETERMINED(""); // leave blank on cover page

    private final String status;

    CcapExpeditedEligibility(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
