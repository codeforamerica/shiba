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
  @Profile({"staging"})
  public Map<String, TribalNationRoutingDestination> stagingTribalNations() {
    Map<String, TribalNationRoutingDestination> result = getDefaultTribalNations();
    addTribalNation(result, MILLE_LACS_BAND_OF_OJIBWE,
        "ccef8f5a-efe1-4ecf-93e2-3b853bea82e6",
        "A602658300",
        "help+staging@mnbenefits.org",
        "320-532-7407",
        true);
    return result;
  }

  @Bean
  @Profile("demo")
  public Map<String, TribalNationRoutingDestination> demoTribalNations() {
    Map<String, TribalNationRoutingDestination> result = getDefaultTribalNations();
    addTribalNation(result,
        MILLE_LACS_BAND_OF_OJIBWE,
        "accef8f5a-efe1-4ecf-93e2-3b853bea82e6",
        "A602658300",
        "help+staging@mnbenefits.org",
        "320-532-7407",
        true);
    addTribalNation(result,
        WHITE_EARTH,
        "4c598f9b-ba43-4077-aee2-b29da2aef79c",
        "A086642300",
        "amy.littlewolf@whiteearth-nsn.gov",
        "218-935-2359",
        false);
    return result;
  }

  @Bean
  @Profile("production")
  public Map<String, TribalNationRoutingDestination> prodTribalNations() {
    Map<String, TribalNationRoutingDestination> result = new HashMap<>();
    addTribalNation(result, MILLE_LACS_BAND_OF_OJIBWE,
        "f30bf89e-f3ab-4c74-8e38-af7ada922719",
        "A602658300",
        "candace.benjamin@millelacsband.com",
        "320-532-7407",
        true);
    addTribalNation(result,
        WHITE_EARTH,
        "d78fd8df-5faf-424c-b05d-f249c11a19cc",
        "A086642300",
        "amy.littlewolf@whiteearth-nsn.gov",
        "218-935-2359",
        false);
    return result;
  }

  @NotNull
  private Map<String, TribalNationRoutingDestination> getDefaultTribalNations() {
    Map<String, TribalNationRoutingDestination> result = new HashMap<>();
    addTribalNation(result,
        MILLE_LACS_BAND_OF_OJIBWE,
        "ae7d7c7f-6503-46ea-92a7-4a813da9fb02",
        "A602658300",
        "help+dev@mnbenefits.org",
        "320-532-7407",
        true);
    result.put(FOND_DU_LAC, new TribalNationRoutingDestination(FOND_DU_LAC, true));
    result.put(GRAND_PORTAGE, new TribalNationRoutingDestination(GRAND_PORTAGE, true));
    result.put(LEECH_LAKE, new TribalNationRoutingDestination(LEECH_LAKE, true));
    addTribalNation(result,
        WHITE_EARTH,
        "3b0aa880-db45-483d-fa0-7987c9b0c02d",
        "A086642300",
        "amy.littlewolf@whiteearth-nsn.gov",
        "218-935-2359",
        false);
    result.put(BOIS_FORTE, new TribalNationRoutingDestination(BOIS_FORTE, true));
    return result;
  }

  private void addTribalNation(Map<String, TribalNationRoutingDestination> result,
      String name, String folderId, String dhsProviderId, String email, String phoneNumber,
      boolean isServicedByMilleLacs) {
    result.put(name,
        new TribalNationRoutingDestination(name, folderId, dhsProviderId, email, phoneNumber,
            isServicedByMilleLacs));
  }
}
