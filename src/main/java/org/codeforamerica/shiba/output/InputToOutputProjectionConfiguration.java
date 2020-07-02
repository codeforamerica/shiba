package org.codeforamerica.shiba.output;

import org.codeforamerica.shiba.YamlPropertySourceFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.HashMap;

@Configuration
@PropertySource(value = "classpath:input-output-config.yaml", factory = YamlPropertySourceFactory.class)
@ConfigurationProperties(prefix = "projections")
public class InputToOutputProjectionConfiguration extends HashMap<String, ProjectionTarget> {
}
