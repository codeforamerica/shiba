package org.codeforamerica.shiba;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.ArrayList;

@Configuration
@PropertySource(value = "classpath:input-output-config.yaml", factory = YamlPropertySourceFactory.class)
@ConfigurationProperties(prefix = "custom-input")
public class CustomInputOutputConfiguration extends ArrayList<String> {
}
