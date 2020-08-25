package org.codeforamerica.shiba;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.HashMap;
import java.util.Map;

@Configuration
@PropertySource(value = "classpath:county-folder-id-mapping.yaml", factory = YamlPropertySourceFactory.class)
public class CountyFolderIdMappingConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "county-to-folder-id")
    Map<County, String> countyFolderIdMapping() {
        return new HashMap<>();
    }
}
