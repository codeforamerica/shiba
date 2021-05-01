package org.codeforamerica.shiba.pages.events;

import lombok.Value;

import java.util.Locale;

@Value
public class UploadedDocumentsSubmittedEvent implements ApplicationEvent {
    String sessionId;
    String applicationId;
    Locale locale;

    @Override
    public InteractionType getInteraction() {
        return InteractionType.UPLOADED_DOCUMENTS;
    }
}
