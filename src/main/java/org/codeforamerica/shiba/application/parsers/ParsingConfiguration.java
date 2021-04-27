package org.codeforamerica.shiba.application.parsers;

import org.codeforamerica.shiba.YamlPropertySourceFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.io.Serial;
import java.util.HashMap;

@Configuration
@PropertySource(value = "classpath:parsing-config.yaml", factory = YamlPropertySourceFactory.class)
@ConfigurationProperties(prefix = "parsing")
public class ParsingConfiguration extends HashMap<String, ParsingCoordinates> {
    @Serial
    private static final long serialVersionUID = 2536065463771955192L;
}
