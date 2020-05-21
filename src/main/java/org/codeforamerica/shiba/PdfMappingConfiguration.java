package org.codeforamerica.shiba;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.HashMap;
import java.util.Map;

@Configuration
@PropertySource(value = "classpath:pdf-mappings.yaml", factory = YamlPropertySourceFactory.class)
public class PdfMappingConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "caf.mappings")
    public Map<String, String> configMap() {
        return new HashMap<>();
    }
}
