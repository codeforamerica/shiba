package org.codeforamerica.shiba.metrics;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.Instant;

@Getter
@EqualsAndHashCode
public class Metrics {
    private Instant startTime;

    public void setStartTimeOnce(Instant instant) {
        if (startTime == null) {
            startTime = instant;
        }
    }
}
