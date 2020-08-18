package org.codeforamerica.shiba.output.applicationinputsmappers;

import org.codeforamerica.shiba.YamlPropertySourceFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.HashMap;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "output-map")
@PropertySource(value = "classpath:output-mapping-configuration.yaml", factory = YamlPropertySourceFactory.class)
public class OutputMappingConfiguration extends HashMap<String, Map<String, Map<String, String>>> {
}
