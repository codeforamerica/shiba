package org.codeforamerica.shiba.output.pdf;

import org.codeforamerica.shiba.YamlPropertySourceFactory;
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
    public Map<String, String> pdfFieldMap() {
        return new HashMap<>();
    }

    @Bean
    @ConfigurationProperties(prefix = "caf.enums")
    public Map<String, String> enumMap() {
        return new HashMap<>();
    }

    @Bean
    public PdfFieldMapper pdfFieldMapper(
            Map<String, String> pdfFieldMap,
            Map<String, String> enumMap
    ) {
        return new PdfFieldMapper(pdfFieldMap, enumMap);
    }
}
