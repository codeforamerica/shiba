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
  private final CountyParser countyParser;
  private final MonitoringService monitoringService;

  public ApplicationFactory(Clock clock, CountyParser countyParser,
      MonitoringService monitoringService) {
    this.clock = clock;
    this.countyParser = countyParser;
    this.monitoringService = monitoringService;
  }

  public Application newApplication(ApplicationData applicationData) {
    ApplicationData copy = new ApplicationData();
    copy.setId(applicationData.getId());
    copy.setPagesData(SerializationUtils.clone(applicationData.getPagesData()));
    copy.setSubworkflows(SerializationUtils.clone(applicationData.getSubworkflows()));
    copy.setIncompleteIterations(applicationData.getIncompleteIterations());
    copy.setFlow(applicationData.getFlow());
    copy.setStartTimeOnce(applicationData.getStartTime());
    copy.setUtmSource(applicationData.getUtmSource());
    monitoringService.setApplicationId(applicationData.getId());

    return Application.builder()
        .id(applicationData.getId())
        .updatedAt(ZonedDateTime.now(clock))
        .applicationData(copy)
        .county(countyParser.parse(applicationData))
        .flow(applicationData.getFlow())
        .build();
  }
}
