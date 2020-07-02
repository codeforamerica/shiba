package org.codeforamerica.shiba.metrics;

import lombok.Getter;

import java.time.Instant;

@Getter
public class Metrics {
    private Instant startTime;

    public void setStartTimeOnce(Instant instant) {
        if (startTime == null) {
            startTime = instant;
        }
    }
}
