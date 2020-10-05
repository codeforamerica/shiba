package org.codeforamerica.shiba.pages.events;

import lombok.Value;

import java.util.Map;

@Value
public class SubworkflowCompletedEvent implements PageEvent {
    String sessionId;
    String groupName;

    @Override
    public InteractionType getInteraction() {
        return InteractionType.SUBWORKFLOW_COMPLETED;
    }

    @Override
    public Map<String, Object> getProperties() { return Map.of("groupName", groupName); }
}
