package org.codeforamerica.shiba;

import io.sentry.Sentry;
import io.sentry.protocol.User;
import org.springframework.stereotype.Component;

@Component
public class SentryClient implements MonitoringService {
    @Override
    public void setApplicationId(String applicationId) {
        Sentry.configureScope(scope -> scope.setContexts("applicationId", applicationId));
    }

    @Override
    public void setSessionId(String sessionId) {
        User user = new User();
        user.setId(sessionId);
        Sentry.setUser(user);
    }

    @Override
    public void setPagesData(String pagesData) {
        Sentry.setExtra("pagesData", pagesData);
    }


}
