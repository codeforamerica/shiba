package org.codeforamerica.shiba.pages;

import lombok.Value;

@Value
public class ApplicationSubmittedEvent {
    String applicationId;

    public ApplicationSubmittedEvent(String applicationId) {
        this.applicationId = applicationId;
    }
}
