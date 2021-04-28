package org.codeforamerica.shiba.pages.events;

import com.mixpanel.mixpanelapi.*;
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
    public void track(String sessionId, String eventName, Map<String, Object> properties) {
        try {
            new MixpanelAPI().sendMessage(messageBuilder.event(sessionId, eventName, new JSONObject(properties)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void trackWithProfile(String sessionId, String eventName, Map<String, Object> properties) {
        try {
            JSONObject event = messageBuilder.event(sessionId, eventName, new JSONObject(properties));
            JSONObject userProfile = messageBuilder.set(sessionId, new JSONObject(properties));

            ClientDelivery delivery = new ClientDelivery();
            delivery.addMessage(event);
            delivery.addMessage(userProfile);
            new MixpanelAPI().deliver(delivery);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
