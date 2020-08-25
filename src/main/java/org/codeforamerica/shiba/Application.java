package org.codeforamerica.shiba;

import lombok.Value;
import org.codeforamerica.shiba.pages.data.ApplicationData;

import java.time.ZonedDateTime;

@Value
public class Application {
    String id;
    ZonedDateTime completedAt;
    ApplicationData applicationData;
    County county;
}
