package org.codeforamerica.shiba;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.HashMap;

@Configuration
@PropertySource(value = "classpath:output-projections-config.yaml", factory = YamlPropertySourceFactory.class)
@ConfigurationProperties(prefix = "projections")
public class InputToOutputProjectionConfiguration extends HashMap<String, ProjectionTarget> {
}
