package org.codeforamerica.shiba;

import io.sentry.Breadcrumb;
import io.sentry.Sentry;
import org.springframework.stereotype.Component;

import static java.util.Optional.ofNullable;

@Component
public class SentryClient implements MonitoringService {
    @Override
    public void setPage(String pageName, String message) {
        Sentry.configureScope(scope -> {
            Breadcrumb breadcrumb = new Breadcrumb(message);
            breadcrumb.setData("pageName", ofNullable(pageName).orElse("null"));
            scope.addBreadcrumb(breadcrumb);
        });
    }

    @Override
    public void setApplicationId(String applicationId) {
        Sentry.configureScope(scope -> scope.setContexts("applicationId", applicationId));
    }

    @Override
    public void sendEvent(String message) {
        Sentry.captureMessage(message);
    }

    @Override
    public void sendException(Exception exception) { Sentry.captureException(exception); }
}
