package org.codeforamerica.shiba.pages.events;

import com.mixpanel.mixpanelapi.MessageBuilder;
import com.mixpanel.mixpanelapi.MixpanelAPI;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
public class MixpanelInteractionTracker implements InteractionTracker {

    private final MessageBuilder messageBuilder;

    public MixpanelInteractionTracker(@Value("${mixpanel.api-key}") String apiToken) {
        messageBuilder = new MessageBuilder(apiToken);
    }

    @Override
    public void track(String sessionId, String event, Map<String, Object> properties) {
        try {
            new MixpanelAPI().sendMessage(messageBuilder.event(sessionId, event, new JSONObject(properties)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
