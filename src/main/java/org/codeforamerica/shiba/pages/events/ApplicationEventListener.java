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
    Application application = applicationRepository.find(event.getApplicationId());
    monitoringService.setApplicationId(application.getId());
    MDC.put("applicationId", application.getId());
    MDC.put("sessionId", event.getSessionId());
    return application;
  }
}
