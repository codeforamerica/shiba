package org.codeforamerica.shiba.pages;

import lombok.Value;
import org.codeforamerica.shiba.application.FlowType;

import java.util.Map;

@Value
public class ApplicationSubmittedEvent implements PageEvent {
    String sessionId;
    String applicationId;
    FlowType flow;

    @Override
    public InteractionType getInteraction() {
        return InteractionType.APPLICATION_SUBMITTED;
    }

    @Override
    public Map<String, Object> getProperties() {
        return Map.of("flow", flow);
    }
}
