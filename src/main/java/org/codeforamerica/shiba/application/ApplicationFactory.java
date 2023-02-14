package org.codeforamerica.shiba.application;

import java.time.Clock;
import java.time.ZonedDateTime;
import org.apache.commons.lang3.SerializationUtils;
import org.codeforamerica.shiba.MonitoringService;
import org.codeforamerica.shiba.application.parsers.CountyParser;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.springframework.stereotype.Component;

@Component
public class ApplicationFactory {

  private final Clock clock;
  private final MonitoringService monitoringService;

  public ApplicationFactory(Clock clock, MonitoringService monitoringService) {
    this.clock = clock;
    this.monitoringService = monitoringService;
  }

  public Application newApplication(ApplicationData applicationData) {
    ApplicationData copy = new ApplicationData();
    copy.setId(applicationData.getId());
    copy.setClientIP(applicationData.getClientIP());
    copy.setPagesData(SerializationUtils.clone(applicationData.getPagesData()));
    copy.setSubworkflows(SerializationUtils.clone(applicationData.getSubworkflows()));
    copy.setIncompleteIterations(applicationData.getIncompleteIterations());
    copy.setFlow(applicationData.getFlow());
    copy.setStartTimeOnce(applicationData.getStartTime());
    copy.setUtmSource(applicationData.getUtmSource());
    copy.setLastPageViewed(applicationData.getLastPageViewed());
    copy.setExpeditedEligibility(applicationData.getExpeditedEligibility());
    copy.setOriginalCounty(applicationData.getOriginalCounty());
    monitoringService.setApplicationId(applicationData.getId());

    return Application.builder()
        .id(applicationData.getId())
        .updatedAt(ZonedDateTime.now(clock))
        .applicationData(copy)
        .county(CountyParser.parse(applicationData))
        .flow(applicationData.getFlow())
        .build();
  }
}
