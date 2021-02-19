package org.codeforamerica.shiba.mnit;

import org.codeforamerica.shiba.CountyMap;
import org.codeforamerica.shiba.YamlPropertySourceFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.*;

@Configuration
@PropertySource(value = "classpath:county-mapping.yaml", factory = YamlPropertySourceFactory.class)
public class CountyMapConfiguration {

    @Bean
    @Profile("default")
    @ConfigurationProperties(prefix = "other")
    CountyMap<MnitCountyInformation> localMapping() {
        return new CountyMap<>();
    }

    @Bean
    @Profile("demo")
    @ConfigurationProperties(prefix = "demo")
    CountyMap<MnitCountyInformation> demoMapping() {
        return new CountyMap<>();
    }

    @Bean
    @Profile("staging")
    @ConfigurationProperties(prefix = "staging")
    CountyMap<MnitCountyInformation> stagingMapping() {
        return new CountyMap<>();
    }

    @Bean
    @Primary
    @Profile("production")
    @ConfigurationProperties(prefix = "production")
    CountyMap<MnitCountyInformation> productionMapping() {
        return new CountyMap<>();
    }
}
