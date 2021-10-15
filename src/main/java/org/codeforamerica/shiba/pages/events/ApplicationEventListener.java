package org.codeforamerica.shiba.pages.events;

import org.codeforamerica.shiba.MonitoringService;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.ApplicationRepository;
import org.jetbrains.annotations.NotNull;
import org.slf4j.MDC;

public abstract class ApplicationEventListener {

  private final ApplicationRepository applicationRepository;
  private final MonitoringService monitoringService;

  protected ApplicationEventListener(ApplicationRepository applicationRepository,
      MonitoringService monitoringService) {
    this.applicationRepository = applicationRepository;
    this.monitoringService = monitoringService;
  }

  @NotNull Application getApplicationFromEvent(ApplicationEvent event) {
    monitoringService.setApplicationId(event.getApplicationId());
    MDC.put("applicationId", event.getApplicationId());
    MDC.put("sessionId", event.getSessionId());
    return applicationRepository.find(event.getApplicationId());
  }
}
