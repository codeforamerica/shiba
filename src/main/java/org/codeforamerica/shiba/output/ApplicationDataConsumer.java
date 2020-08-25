package org.codeforamerica.shiba.output;

import org.codeforamerica.shiba.Application;

public interface ApplicationDataConsumer {
    void process(Application application);
}
