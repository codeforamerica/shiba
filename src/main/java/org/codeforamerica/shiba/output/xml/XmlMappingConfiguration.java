package org.codeforamerica.shiba.output.xml;

import java.util.HashMap;
import java.util.Map;
import org.codeforamerica.shiba.YamlPropertySourceFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value = "classpath:xml-mappings.yaml", factory = YamlPropertySourceFactory.class)
public class XmlMappingConfiguration {

  @Bean
  @ConfigurationProperties(prefix = "xml.mappings")
  Map<String, String> xmlConfigMap() {
    return new HashMap<>();
  }

  @Bean
  @ConfigurationProperties(prefix = "xml.enums")
  Map<String, String> xmlEnum() {
    return new HashMap<>();
  }
}
