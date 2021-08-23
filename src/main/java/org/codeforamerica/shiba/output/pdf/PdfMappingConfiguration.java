package org.codeforamerica.shiba.output.pdf;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.codeforamerica.shiba.YamlPropertySourceFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value = "classpath:pdf-mappings.yaml", factory = YamlPropertySourceFactory.class)
public class PdfMappingConfiguration {

  @Bean
  @ConfigurationProperties(prefix = "mappings")
  public Map<String, List<String>> pdfFieldMap() {
    return new HashMap<>();
  }

  @Bean
  @ConfigurationProperties(prefix = "enums")
  public Map<String, String> enumMap() {
    return new HashMap<>();
  }

  @Bean
  public PdfFieldMapper pdfFieldMapper(
      Map<String, List<String>> pdfFieldMap,
      Map<String, String> enumMap
  ) {
    return new PdfFieldMapper(pdfFieldMap, enumMap);
  }
}
