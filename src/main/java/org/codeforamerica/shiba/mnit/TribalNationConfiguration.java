package org.codeforamerica.shiba.mnit;

import static org.codeforamerica.shiba.TribalNation.BOIS_FORTE;
import static org.codeforamerica.shiba.TribalNation.FOND_DU_LAC;
import static org.codeforamerica.shiba.TribalNation.GRAND_PORTAGE;
import static org.codeforamerica.shiba.TribalNation.LEECH_LAKE;
import static org.codeforamerica.shiba.TribalNation.MILLE_LACS_BAND_OF_OJIBWE;
import static org.codeforamerica.shiba.TribalNation.WHITE_EARTH;

import java.util.HashMap;
import java.util.Map;
import org.codeforamerica.shiba.TribalNation;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class TribalNationConfiguration {

  @Bean
  @Profile({"test", "default"})
  public Map<String, TribalNation> localTribalNations() {
    return getDefaultTribalNations();
  }

  @Bean
  @Profile({"staging"})
  public Map<String, TribalNation> stagingTribalNations() {
    Map<String, TribalNation> result = getDefaultTribalNations();
    result.put(MILLE_LACS_BAND_OF_OJIBWE, new TribalNation(
        MILLE_LACS_BAND_OF_OJIBWE,
        "ccef8f5a-efe1-4ecf-93e2-3b853bea82e6",
        "A602658300",
        "help+staging@mnbenefits.org",
        "320-532-7407",
        true));
    return result;
  }

  @Bean
  @Profile("demo")
  public Map<String, TribalNation> demoTribalNations() {
    Map<String, TribalNation> result = getDefaultTribalNations();
    result.put(MILLE_LACS_BAND_OF_OJIBWE, new TribalNation(
        MILLE_LACS_BAND_OF_OJIBWE,
        "accef8f5a-efe1-4ecf-93e2-3b853bea82e6",
        "A602658300",
        "help+staging@mnbenefits.org",
        "320-532-7407",
        true));
    return result;
  }

  @Bean
  @Profile("production")
  public Map<String, TribalNation> prodTribalNations() {
    Map<String, TribalNation> result = new HashMap<>();
    result.put(MILLE_LACS_BAND_OF_OJIBWE, new TribalNation(
        MILLE_LACS_BAND_OF_OJIBWE,
        "f30bf89e-f3ab-4c74-8e38-af7ada922719",
        "A602658300",
        "candace.benjamin@millelacsband.com",
        "320-532-7407",
        true));
    return result;
  }

  @NotNull
  private Map<String, TribalNation> getDefaultTribalNations() {
    Map<String, TribalNation> result = new HashMap<>();
    result.put(MILLE_LACS_BAND_OF_OJIBWE, new TribalNation(
        MILLE_LACS_BAND_OF_OJIBWE,
        "ae7d7c7f-6503-46ea-92a7-4a813da9fb02",
        "A602658300",
        "help+dev@mnbenefits.org",
        "320-532-7407",
        true));
    result.put(FOND_DU_LAC, new TribalNation(
        FOND_DU_LAC,
        true
    ));
    result.put(GRAND_PORTAGE, new TribalNation(
        GRAND_PORTAGE,
        true
    ));
    result.put(LEECH_LAKE, new TribalNation(
        LEECH_LAKE,
        true
    ));
    result.put(WHITE_EARTH, new TribalNation(
        WHITE_EARTH,
        true
    ));
    result.put(BOIS_FORTE, new TribalNation(
        BOIS_FORTE,
        true
    ));
    return result;
  }
}
