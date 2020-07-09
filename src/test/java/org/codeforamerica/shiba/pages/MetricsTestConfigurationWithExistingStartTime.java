package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.metrics.Metrics;
import org.springframework.context.annotation.Bean;

import java.time.Instant;

public class MetricsTestConfigurationWithExistingStartTime {
    @Bean
    public Metrics metrics() {
        Metrics metrics = new Metrics();
        metrics.setStartTimeOnce(Instant.now());
        return metrics;
    }
}
