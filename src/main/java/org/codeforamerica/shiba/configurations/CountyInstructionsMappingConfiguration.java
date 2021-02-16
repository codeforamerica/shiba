package org.codeforamerica.shiba.configurations;

import org.codeforamerica.shiba.CountyMap;
import org.codeforamerica.shiba.YamlPropertySourceFactory;
import org.codeforamerica.shiba.output.Recipient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.Map;

@Configuration
@PropertySource(value = "classpath:county-instructions-mapping.yaml", factory = YamlPropertySourceFactory.class)
public class CountyInstructionsMappingConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "county-to-instructions")
    CountyMap<Map<Recipient, String>> countyInstructionsMapping() {
        return new CountyMap<>();
    }
}
