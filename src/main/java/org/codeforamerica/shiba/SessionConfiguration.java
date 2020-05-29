package org.codeforamerica.shiba;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.context.annotation.ScopedProxyMode.TARGET_CLASS;
import static org.springframework.web.context.WebApplicationContext.SCOPE_SESSION;

@Configuration
@PropertySource(value = "classpath:screens-config.yaml", factory = YamlPropertySourceFactory.class)
public class SessionConfiguration {
    @Bean
//    @Scope(
//            value = SCOPE_SESSION,
//            proxyMode = TARGET_CLASS
//    )
    public BenefitsApplication benefitsApplication() {
        return new BenefitsApplication();
    }

    @Bean
    @Scope(
            value = SCOPE_SESSION,
            proxyMode = TARGET_CLASS
    )
    @ConfigurationProperties(prefix = "screens")
    public Screens screens() {
        return new Screens();
    }
}
