package org.codeforamerica.shiba.pages.events;

import java.util.Map;

public interface PageEvent {

  String getSessionId();

  InteractionType getInteraction();

  default Map<String, Object> getProperties() {
    return Map.of();
  }
}
