package org.codeforamerica.shiba.pages.events;

import java.util.Locale;
import lombok.Value;

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
