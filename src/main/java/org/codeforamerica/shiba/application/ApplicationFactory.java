package org.codeforamerica.shiba.application;

import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.application.parsers.ApplicationDataParser;
import org.codeforamerica.shiba.metrics.Metrics;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.ZonedDateTime;

@Component
public class ApplicationFactory {
    private final Clock clock;
    private final ApplicationDataParser<County> countyParser;

    public ApplicationFactory(
            Clock clock,
            ApplicationDataParser<County> countyParser) {
        this.clock = clock;
        this.countyParser = countyParser;
    }

    public Application newApplication(String id, ApplicationData applicationData, Metrics metrics) {
        ApplicationData copy = new ApplicationData();
        copy.setPagesData(applicationData.getPagesData());
        copy.setSubworkflows(applicationData.getSubworkflows());
        copy.setIncompleteIterations(applicationData.getIncompleteIterations());
        copy.setFlow(applicationData.getFlow());
        ZonedDateTime completedAt = ZonedDateTime.now(clock);

        return Application.builder()
                .id(id)
                .completedAt(completedAt)
                .applicationData(copy)
                .county(countyParser.parse(applicationData))
                .timeToComplete(Duration.between(metrics.getStartTime(), completedAt))
                .flow(applicationData.getFlow())
                .build();
    }
}
