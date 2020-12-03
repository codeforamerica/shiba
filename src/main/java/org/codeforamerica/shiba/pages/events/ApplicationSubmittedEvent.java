package org.codeforamerica.shiba.pages.events;

import lombok.Value;
import org.codeforamerica.shiba.application.FlowType;

import java.util.Locale;
import java.util.Map;

@Value
public class ApplicationSubmittedEvent implements PageEvent {
    String sessionId;
    String applicationId;
    FlowType flow;
    Locale locale;

    @Override
    public InteractionType getInteraction() {
        return InteractionType.APPLICATION_SUBMITTED;
    }

    @Override
    public Map<String, Object> getProperties() {
        return Map.of("flow", flow);
    }
}
