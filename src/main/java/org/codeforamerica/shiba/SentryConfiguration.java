package org.codeforamerica.shiba;

import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.HandlerExceptionResolver;

public class SentryConfiguration {
    @Bean
    public HandlerExceptionResolver sentryExceptionResolver() {
        return new io.sentry.spring.SentryExceptionResolver();
    }
}
