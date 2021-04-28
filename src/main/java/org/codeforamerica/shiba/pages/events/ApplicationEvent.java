package org.codeforamerica.shiba.pages.events;

public interface ApplicationEvent extends PageEvent {
    String getApplicationId();
    String getSessionId();
}
