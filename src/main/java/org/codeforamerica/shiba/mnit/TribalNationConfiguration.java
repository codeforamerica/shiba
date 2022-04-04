package org.codeforamerica.shiba.mnit;

import static org.codeforamerica.shiba.TribalNationRoutingDestination.*;

import java.util.HashMap;
import java.util.Map;
import org.codeforamerica.shiba.TribalNationRoutingDestination;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class TribalNationConfiguration {

  @Bean
  @Profile({"test", "default"})
  public Map<String, TribalNationRoutingDestination> localTribalNations() {
    return getDefaultTribalNations();
  }

  @Bean
  @Profile({"dev"})
  public Map<String, TribalNationRoutingDestination> stagingTribalNations() {
    Map<String, TribalNationRoutingDestination> result = getDefaultTribalNations();
    addTribalNation(result, MILLE_LACS_BAND_OF_OJIBWE,
        "A602658300",
        "help+staging@mnbenefits.org",
        "320-532-7407"
    );

    addTribalNation(result, WHITE_EARTH,
        "A086642300",
        "help+staging@mnbenefits.org",
        "218-935-2359"
    );
    addTribalNation(result, RED_LAKE_NATION,
        "A590043300",
        "help+staging@mnbenefits.org",
        "218-679-3350"
    );
    return result;
  }

  @Bean
  @Profile("atst")
  public Map<String, TribalNationRoutingDestination> demoTribalNations() {
    Map<String, TribalNationRoutingDestination> result = getDefaultTribalNations();
    addTribalNation(result,
        MILLE_LACS_BAND_OF_OJIBWE,
        "A602658300",
        "help+demo@mnbenefits.org",
        "320-532-7407"
    );
    addTribalNation(result,
        WHITE_EARTH,
        "A086642300",
        "help+demo@mnbenefits.org",
        "218-935-2359"
    );
    addTribalNation(result, RED_LAKE_NATION,
        "A590043300",
        "help+demo@mnbenefits.org",
        "218-679-3350"
    );
    return result;
  }

  @Bean
  @Profile("production")
  public Map<String, TribalNationRoutingDestination> prodTribalNations() {
    Map<String, TribalNationRoutingDestination> result = new HashMap<>();
    addTribalNation(result, MILLE_LACS_BAND_OF_OJIBWE,
        "A602658300",
        "candace.benjamin@millelacsband.com",
        "320-532-7407"
    );
    addTribalNation(result,
        WHITE_EARTH,
        "A086642300",
        "amy.littlewolf@whiteearth-nsn.gov",
        "218-935-2359"
    );
    addTribalNation(result, RED_LAKE_NATION,
        "A590043300",
        "sarah.smythe@redlakenation.org",
        "218-679-3350"
    );
    return result;
  }

  @NotNull
  private Map<String, TribalNationRoutingDestination> getDefaultTribalNations() {
    Map<String, TribalNationRoutingDestination> result = new HashMap<>();
    addTribalNation(result, MILLE_LACS_BAND_OF_OJIBWE,
        "A602658300",
        "help+dev@mnbenefits.org",
        "320-532-7407"
    );
    addTribalNation(result, FOND_DU_LAC);
    addTribalNation(result, GRAND_PORTAGE);
    addTribalNation(result, LEECH_LAKE);
    addTribalNation(result, WHITE_EARTH,
        "A086642300",
        "help+dev@mnbenefits.org",
        "218-935-2359"
    );
    addTribalNation(result, BOIS_FORTE);
    addTribalNation(result, RED_LAKE_NATION,
        "A590043300",
        "help+dev@mnbenefits.org",
        "218-679-3350"
    );
    return result;
  }

  private void addTribalNation(Map<String, TribalNationRoutingDestination> map,
      String name) {
    map.put(name, new TribalNationRoutingDestination(name));
  }

  private void addTribalNation(Map<String, TribalNationRoutingDestination> result,
      String name, String dhsProviderId, String email, String phoneNumber) {
    result.put(name,
        new TribalNationRoutingDestination(name, dhsProviderId, email, phoneNumber));
  }
}
