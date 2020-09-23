package org.codeforamerica.shiba.pages;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class SpringApplicationEventPageEventPublisher implements PageEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public SpringApplicationEventPageEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void publish(PageEvent pageEvent) {
        applicationEventPublisher.publishEvent(pageEvent);
    }
}
