package org.codeforamerica.shiba;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.context.annotation.ScopedProxyMode.TARGET_CLASS;
import static org.springframework.web.context.WebApplicationContext.SCOPE_SESSION;

@Configuration
public class SessionConfiguration {
    @Bean
    @Scope(
            value = SCOPE_SESSION,
            proxyMode = TARGET_CLASS
    )
    public Map<String, FormData> data() {
        return new HashMap<>();
    }
}
