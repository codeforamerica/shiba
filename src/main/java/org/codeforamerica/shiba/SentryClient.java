package org.codeforamerica.shiba;

import io.sentry.Breadcrumb;
import io.sentry.Sentry;
import org.springframework.stereotype.Component;

import static java.util.Optional.ofNullable;

@Component
public class SentryClient implements MonitoringService {

    @Override
    public void setPage(String pageName, String message) {
        // Something about this is busted
        // How to unbust?
        Sentry.configureScope(scope -> {
            Breadcrumb breadcrumb = new Breadcrumb(message);
            breadcrumb.setData("pageName", ofNullable(pageName).orElse("null"));
            scope.addBreadcrumb(breadcrumb);
        });
    }

    @Override
    public void setApplicationId(String applicationId) {
        // This seems to be setting a global config used for all sessions
        // we want it to only apply to our current session
        Sentry.configureScope(scope -> scope.setContexts("applicationId", applicationId));
    }

    @Override
    public void sendEvent(String message) {
        // could just add all the enriched data right here maybe?
        Sentry.captureMessage(message);
    }

    @Override
    public void sendException(Exception exception) {
        // could just add all the enriched data here too
        Sentry.captureException(exception);
    }
}
