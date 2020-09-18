package org.codeforamerica.shiba.output;

import org.codeforamerica.shiba.application.Application;

public interface ApplicationDataConsumer {
    void process(Application application);
}
