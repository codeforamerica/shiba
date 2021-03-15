package org.codeforamerica.shiba;

import io.sentry.Sentry;
import org.springframework.stereotype.Component;

@Component
public class SentryClient implements MonitoringService {
    @Override
    public void setApplicationId(String applicationId) {
        Sentry.configureScope(scope -> scope.setContexts("applicationId", applicationId));
    }
}
