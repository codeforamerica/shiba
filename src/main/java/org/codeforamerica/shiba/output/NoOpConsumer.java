package org.codeforamerica.shiba.output;

import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.ZonedDateTime;

@Component
public class NoOpConsumer implements ApplicationDataConsumer {
    private final Clock clock;

    public NoOpConsumer(Clock clock) {
        this.clock = clock;
    }

    @Override
    public ZonedDateTime process(ApplicationData applicationData) {
        return ZonedDateTime.now(clock);
    }
}
