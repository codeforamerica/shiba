package org.codeforamerica.shiba.mnit;

import java.util.ArrayList;
import org.codeforamerica.shiba.TribalNation;
import org.codeforamerica.shiba.YamlPropertySourceFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ConfigurationProperties(prefix = "tribalnations")
@PropertySource(value = "classpath:tribal-nation-mapping.yaml", factory = YamlPropertySourceFactory.class)
public class TribalNationConfiguration extends ArrayList<TribalNation> {

}
