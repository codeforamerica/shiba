package org.codeforamerica.shiba.pages.events;

import lombok.Value;

@Value
public class UploadedDocumentsSubmittedEvent implements ApplicationEvent {
    String sessionId;
    String applicationId;

    @Override
    public InteractionType getInteraction() {
        return InteractionType.UPLOADED_DOCUMENTS;
    }
}
