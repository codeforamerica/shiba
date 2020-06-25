package org.codeforamerica.shiba;

import lombok.Getter;

import java.time.Instant;

@Getter
public class SessionMetadata {
    private Instant startTime;

    public void setStartTimeOnce(Instant instant) {
        if (startTime == null) {
            startTime = instant;
        }
    }
}
