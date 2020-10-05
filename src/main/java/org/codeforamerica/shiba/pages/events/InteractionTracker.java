package org.codeforamerica.shiba.pages.events;

import java.util.Map;

public interface InteractionTracker {
    void track(String sessionId, String event, Map<String, Object> properties);
}
