package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.InteractionTracker;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class PageEventListener {

    private final InteractionTracker interactionTracker;

    public PageEventListener(InteractionTracker interactionTracker) {
        this.interactionTracker = interactionTracker;
    }

    @Async
    @EventListener
    public void captureInteraction(PageEvent pageEvent) {
        interactionTracker.track(pageEvent.getSessionId(), pageEvent.getInteraction().name(), pageEvent.getProperties());
    }
}
