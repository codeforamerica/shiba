package org.codeforamerica.shiba.pages.events;

import lombok.Value;

@Value
public class UploadedDocumentsSubmittedEvent implements PageEvent {
    String sessionId;
    String applicationId;

    @Override
    public InteractionType getInteraction() {
        return InteractionType.APPLICATION_SUBMITTED;
    }
}
