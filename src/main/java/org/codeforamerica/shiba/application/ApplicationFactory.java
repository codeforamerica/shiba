package org.codeforamerica.shiba.application;

import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.MonitoringService;
import org.codeforamerica.shiba.application.parsers.ApplicationDataParser;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.ZonedDateTime;

@Component
public class ApplicationFactory {
    private final Clock clock;
    private final ApplicationDataParser<County> countyParser;
    private final MonitoringService monitoringService;

    public ApplicationFactory(Clock clock, ApplicationDataParser<County> countyParser, MonitoringService monitoringService) {
        this.clock = clock;
        this.countyParser = countyParser;
        this.monitoringService = monitoringService;
    }

    public Application newApplication(ApplicationData applicationData) {
        ApplicationData copy = new ApplicationData();
        copy.setId(applicationData.getId());
        copy.setPagesData(applicationData.getPagesData());
        copy.setSubworkflows(applicationData.getSubworkflows());
        copy.setIncompleteIterations(applicationData.getIncompleteIterations());
        copy.setFlow(applicationData.getFlow());
        copy.setStartTime(applicationData.getStartTime());
        copy.setUtmSource(applicationData.getUtmSource());
        ZonedDateTime completedAt = ZonedDateTime.now(clock);
        monitoringService.setApplicationId(applicationData.getId());

        return Application.builder()
                .id(applicationData.getId())
                .completedAt(completedAt)
                .applicationData(copy)
                .county(countyParser.parse(applicationData))
                .timeToComplete(Duration.between(applicationData.getStartTime(), completedAt))
                .flow(applicationData.getFlow())
                .build();
    }
}
