package org.codeforamerica.shiba.output;

import org.codeforamerica.shiba.pages.data.ApplicationData;

import java.time.ZonedDateTime;

public interface ApplicationDataConsumer {
    ZonedDateTime process(ApplicationData applicationData);
}
