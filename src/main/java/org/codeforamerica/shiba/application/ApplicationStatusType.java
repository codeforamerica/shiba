package org.codeforamerica.shiba.application;

public enum ApplicationStatusType {
    CAF("caf_application_status"),
    CCAP("ccap_application_status"),
    UPLOADED_DOCUMENTS("uploaded_documents_status");

    private final String displayName;

    ApplicationStatusType(String displayName) {
        this.displayName = displayName;
    }

    public String displayName(){
        return displayName;
    }


    @Override
    public String toString() {
        return displayName;
    }
}