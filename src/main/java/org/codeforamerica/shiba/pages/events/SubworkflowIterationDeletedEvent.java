package org.codeforamerica.shiba.pages.events;

import java.util.Map;
import lombok.Value;

@Value
public class SubworkflowIterationDeletedEvent implements PageEvent {

  String sessionId;
  String groupName;

  @Override
  public InteractionType getInteraction() {
    return InteractionType.SUBWORKFLOW_ITERATION_DELETED;
  }

  @Override
  public Map<String, Object> getProperties() {
    return Map.of("groupName", groupName);
  }
}
