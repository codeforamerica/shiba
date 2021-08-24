package org.codeforamerica.shiba.output;

import java.io.Serial;
import java.util.ArrayList;
import org.codeforamerica.shiba.YamlPropertySourceFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ConfigurationProperties(prefix = "derived-values")
@PropertySource(value = "classpath:derived-value-config.yaml", factory = YamlPropertySourceFactory.class)
public class PotentialDerivedValuesConfiguration extends ArrayList<PotentialDerivedValues> {

  @Serial
  private static final long serialVersionUID = 6562147178455720859L;
}
