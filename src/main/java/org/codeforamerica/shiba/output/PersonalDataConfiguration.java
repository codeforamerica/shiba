package org.codeforamerica.shiba.output;

import java.util.HashMap;
import java.util.Map;
import org.codeforamerica.shiba.YamlPropertySourceFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value = "classpath:personal-data-mappings.yaml", factory = YamlPropertySourceFactory.class)
public class PersonalDataConfiguration {

  @Bean
  @ConfigurationProperties(prefix = "personal-data")
  Map<String, String> personalDataMappings() {
    return new HashMap<>();
  }
}
