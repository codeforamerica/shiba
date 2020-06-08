package org.codeforamerica.shiba;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;


@Configuration
@PropertySource(value = "classpath:screens-config.yaml", factory = YamlPropertySourceFactory.class)
public class ScreenConfiguration {
    @Bean
    @ConfigurationProperties(prefix = "screens")
    public Screens screens() {
        return new Screens();
    }
}
