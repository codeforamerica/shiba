package org.codeforamerica.shiba.pages.events;

import java.util.Map;

public interface InteractionTracker {
    void track(String sessionId, String eventName, Map<String, Object> properties);
    void trackWithProfile(String sessionId, String eventName, Map<String, Object> properties);
}
