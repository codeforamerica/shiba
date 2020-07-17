package org.codeforamerica.shiba.output;

import org.codeforamerica.shiba.YamlPropertySourceFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.HashMap;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "derived-values")
@PropertySource(value = "classpath:derived-value-config.yaml", factory = YamlPropertySourceFactory.class)
public class DerivedValueConfiguration extends HashMap<String, Map<String, DerivedValue>> {
}
