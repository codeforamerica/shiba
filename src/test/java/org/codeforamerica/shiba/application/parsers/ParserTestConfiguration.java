package org.codeforamerica.shiba.application.parsers;

import org.codeforamerica.shiba.YamlPropertySourceFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;

@TestConfiguration
@PropertySource(value = "classpath:test-parsing-config.yaml", factory = YamlPropertySourceFactory.class)
public class ParserTestConfiguration {
    @SuppressWarnings("ConfigurationProperties")
    @Bean
    @ConfigurationProperties(prefix = "test-parsing")
    public ParsingConfiguration parsingConfiguration() {
        return new ParsingConfiguration();
    }
}
