package org.codeforamerica.shiba;

import java.util.Map;

public interface InteractionTracker {
    void track(String sessionId, String event, Map<String, Object> properties);
}
