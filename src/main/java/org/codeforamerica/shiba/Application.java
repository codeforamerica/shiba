package org.codeforamerica.shiba;

import lombok.Builder;
import lombok.Value;
import org.codeforamerica.shiba.pages.data.ApplicationData;

import java.time.Duration;
import java.time.ZonedDateTime;

@Value
@Builder
public class Application {
    String id;
    ZonedDateTime completedAt;
    ApplicationData applicationData;
    County county;
    String fileName;
    Duration timeToComplete;
}
