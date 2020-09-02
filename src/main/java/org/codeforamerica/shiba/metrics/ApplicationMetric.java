package org.codeforamerica.shiba.metrics;

import lombok.Value;
import org.codeforamerica.shiba.County;

import java.time.Duration;
import java.time.ZonedDateTime;

@Value
public class ApplicationMetric {
    Duration timeToComplete;
    County county;
    ZonedDateTime completedAt;
}
