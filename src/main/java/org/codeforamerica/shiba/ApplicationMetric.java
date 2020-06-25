package org.codeforamerica.shiba;

import lombok.Value;

import java.time.Duration;

@Value
public class ApplicationMetric {
    Duration timeToComplete;
}
