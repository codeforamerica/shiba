package org.codeforamerica.shiba.pages.events;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
@Slf4j
@Component
public class SpringApplicationEventPageEventPublisher implements PageEventPublisher {

  private final ApplicationEventPublisher applicationEventPublisher;

  public SpringApplicationEventPageEventPublisher(
      ApplicationEventPublisher applicationEventPublisher) {
    this.applicationEventPublisher = applicationEventPublisher;
  }

  @Override
  public void publish(PageEvent pageEvent) {
    applicationEventPublisher.publishEvent(pageEvent);
    log.info("Published Event: " + pageEvent.toString());
  }
}
