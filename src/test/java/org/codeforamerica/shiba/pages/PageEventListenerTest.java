package org.codeforamerica.shiba.pages;

import static org.codeforamerica.shiba.pages.events.InteractionType.APPLICATION_SUBMITTED;
import static org.codeforamerica.shiba.pages.events.InteractionType.SUBWORKFLOW_COMPLETED;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Locale;
import java.util.Map;
import org.codeforamerica.shiba.application.FlowType;
import org.codeforamerica.shiba.pages.events.ApplicationSubmittedEvent;
import org.codeforamerica.shiba.pages.events.InteractionTracker;
import org.codeforamerica.shiba.pages.events.InteractionType;
import org.codeforamerica.shiba.pages.events.PageEvent;
import org.codeforamerica.shiba.pages.events.PageEventListener;
import org.junit.jupiter.api.Test;

class PageEventListenerTest {

  private final InteractionTracker interactionTracker = mock(InteractionTracker.class);

  private final PageEventListener pageEventListener = new PageEventListener(interactionTracker);

  @Test
  void shouldTrackInteractions() {
    PageEvent testPageEvent = new PageEvent() {
      @Override
      public String getSessionId() {
        return "someSessionId";
      }

      @Override
      public InteractionType getInteraction() {
        return SUBWORKFLOW_COMPLETED;
      }

      @Override
      public Map<String, Object> getProperties() {
        return Map.of("somePropertyKey", "somePropertyValue");
      }
    };

    pageEventListener.captureInteraction(testPageEvent);

    verify(interactionTracker).track(
        "someSessionId",
        SUBWORKFLOW_COMPLETED.name(),
        Map.of("somePropertyKey", "somePropertyValue"));
  }

  @Test
  void shouldTrackApplicationSubmittedEvents() {
    ApplicationSubmittedEvent applicationSubmittedEvent = new ApplicationSubmittedEvent(
        "sessionId", "applicationId", FlowType.FULL, Locale.ENGLISH
    );

    pageEventListener.captureInteraction(applicationSubmittedEvent);
    verify(interactionTracker).trackWithProfile(
        "sessionId",
        APPLICATION_SUBMITTED.name(),
        Map.of("flow", FlowType.FULL, "confirmation #", "applicationId")
    );
  }
}
