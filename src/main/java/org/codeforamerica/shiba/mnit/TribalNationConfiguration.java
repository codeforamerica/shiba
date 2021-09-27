package org.codeforamerica.shiba.mnit;

import static org.codeforamerica.shiba.TribalNation.MILLE_LACS_BAND_OF_OJIBWE;

import java.util.HashMap;
import java.util.Map;
import org.codeforamerica.shiba.TribalNation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
//@ConfigurationProperties(prefix = "tribalnations") // TODO remove if we don't use
//@PropertySource(value = "classpath:tribal-nation-mapping.yaml", factory = YamlPropertySourceFactory.class)
public class TribalNationConfiguration extends HashMap<String, TribalNation> {

  @Bean
  @Profile({"default", "test"})
  public Map<String, TribalNation> localTribalNations() {
    Map<String, TribalNation> result = new HashMap<>();
    result.put(MILLE_LACS_BAND_OF_OJIBWE, new TribalNation(
        MILLE_LACS_BAND_OF_OJIBWE, // TODO annoying but maybe the less bad option
        "ae7d7c7f-6503-46ea-92a7-4a813da9fb02",
        "A602658300",
        "help+dev@mnbenefits.org",
        "320-532-7407",
        true));
    return result;
  }

  @Bean
  @Profile("production")
  public Map<String, TribalNation> tribalNations() {
    Map<String, TribalNation> result = new HashMap<>();
    result.put(MILLE_LACS_BAND_OF_OJIBWE, new TribalNation(
        MILLE_LACS_BAND_OF_OJIBWE, // TODO annoying but maybe the less bad option
        "ae7d7c7f-6503-46ea-92a7-4a813da9fb02",
        "A602658300",
        "help+dev@mnbenefits.org",
        "320-532-7407",
        true));
    return result;
  }
}
