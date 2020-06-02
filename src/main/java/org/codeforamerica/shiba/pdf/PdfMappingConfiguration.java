package org.codeforamerica.shiba.pdf;

import org.codeforamerica.shiba.YamlPropertySourceFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Configuration
@PropertySource(value = "classpath:pdf-mappings.yaml", factory = YamlPropertySourceFactory.class)
public class PdfMappingConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "caf.mappings")
    public Map<String, String> pdfFieldMap() {
        return new HashMap<>();
    }

    @Bean
    public Set<String> valueExclusions() {
        return Set.of("RATHER_NOT_SAY");
    }
}
