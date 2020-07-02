package org.codeforamerica.shiba.metrics;

import lombok.Value;

import java.time.Duration;

@Value
public class ApplicationMetric {
    Duration timeToComplete;
}
