package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.YamlPropertySourceFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value = "classpath:county-email-mapping.yaml", factory = YamlPropertySourceFactory.class)
public class CountyEmailMapConfiguration {

    @Bean
    @Profile("!(demo | staging | production)")
    @ConfigurationProperties(prefix = "other")
    CountyEmailMap localMapping() {
        return new CountyEmailMap();
    }

    @Bean
    @Profile("demo")
    @ConfigurationProperties(prefix = "demo")
    CountyEmailMap demoMapping() {
        return new CountyEmailMap();
    }

    @Bean
    @Profile("staging")
    @ConfigurationProperties(prefix = "staging")
    CountyEmailMap stagingMapping() {
        return new CountyEmailMap();
    }

    @Bean
    @Profile("production")
    @ConfigurationProperties(prefix = "production")
    CountyEmailMap productionMapping() {
        return new CountyEmailMap();
    }
}
