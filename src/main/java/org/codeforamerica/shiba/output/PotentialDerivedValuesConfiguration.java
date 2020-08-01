package org.codeforamerica.shiba.output;

import org.codeforamerica.shiba.YamlPropertySourceFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.ArrayList;

@Configuration
@ConfigurationProperties(prefix = "derived-values")
@PropertySource(value = "classpath:derived-value-config.yaml", factory = YamlPropertySourceFactory.class)
public class PotentialDerivedValuesConfiguration extends ArrayList<PotentialDerivedValues> {
}
