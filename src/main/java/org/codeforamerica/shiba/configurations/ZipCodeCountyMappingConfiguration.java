package org.codeforamerica.shiba.configurations;

import java.util.HashMap;
import java.util.Map;
import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.YamlPropertySourceFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value = "classpath:zip-to-county-mapping.yaml", factory = YamlPropertySourceFactory.class)
public class ZipCodeCountyMappingConfiguration {

  @Bean
  @ConfigurationProperties(prefix = "zip-code-to-county")
  public Map<String, County> zipCodeCountyMapping() {
    return new HashMap<>();
  }
}
