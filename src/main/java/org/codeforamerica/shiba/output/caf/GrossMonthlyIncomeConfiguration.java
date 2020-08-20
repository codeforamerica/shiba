package org.codeforamerica.shiba.output.caf;

import lombok.Data;
import org.codeforamerica.shiba.YamlPropertySourceFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.HashMap;
import java.util.Map;

@Configuration
@PropertySource(value = "classpath:gross-monthly-income-config.yaml", factory = YamlPropertySourceFactory.class)
@ConfigurationProperties(prefix = "gross-monthly-income")
@Data
public class GrossMonthlyIncomeConfiguration {
    String groupName;
    Map<String, PageInputCoordinates> pageInputs = new HashMap<>();
}
