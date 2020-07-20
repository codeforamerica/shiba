package org.codeforamerica.shiba.output.caf;

import org.codeforamerica.shiba.YamlPropertySourceFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.HashMap;

@Configuration
@PropertySource(value = "classpath:expedited-eligibility-config.yaml", factory = YamlPropertySourceFactory.class)
@ConfigurationProperties(prefix = "expedited-eligibility")
public class ExpeditedEligibilityConfiguration extends HashMap<String, PageInputCoordinates> {
}
