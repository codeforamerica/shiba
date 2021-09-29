package org.codeforamerica.shiba.mnit;

import org.codeforamerica.shiba.CountyMap;
import org.codeforamerica.shiba.YamlPropertySourceFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value = "classpath:county-mapping.yaml", factory = YamlPropertySourceFactory.class)
public class CountyMapConfiguration {

  @Bean
  @Profile({"default", "test"})
  @ConfigurationProperties(prefix = "other")
  CountyMap<CountyRoutingDestination> localMapping() {
    return new CountyMap<>();
  }

  @Bean
  @Profile("demo")
  @ConfigurationProperties(prefix = "demo")
  CountyMap<CountyRoutingDestination> demoMapping() {
    return new CountyMap<>();
  }

  @Bean
  @Profile("staging")
  @ConfigurationProperties(prefix = "staging")
  CountyMap<CountyRoutingDestination> stagingMapping() {
    return new CountyMap<>();
  }

  @Bean
  @Profile("production")
  @ConfigurationProperties(prefix = "production")
  CountyMap<CountyRoutingDestination> productionMapping() {
    return new CountyMap<>();
  }
}
