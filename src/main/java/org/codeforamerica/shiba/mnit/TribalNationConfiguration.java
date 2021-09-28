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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
//@ConfigurationProperties(prefix = "tribalnations") // TODO remove if we don't use
//@PropertySource(value = "classpath:tribal-nation-mapping.yaml", factory = YamlPropertySourceFactory.class)
public class TribalNationConfiguration {
// staging
  // MilleLacsBand:
//      phoneNumber: 320-532-7407
//      email: help+staging@mnbenefits.org
//      folderId: ccef8f5a-efe1-4ecf-93e2-3b853bea82e6
//      dhsProviderId: A602658300

  // demo
  //MilleLacsBand:
  //      email: help+staging@mnbenefits.org
  //      folderId: ccef8f5a-efe1-4ecf-93e2-3b853bea82e6
  //      dhsProviderId: A602658300
  //      phoneNumber: 320-532-7407

  // prod
  //  MilleLacsBand:
  //      folderId: f30bf89e-f3ab-4c74-8e38-af7ada922719
  //      dhsProviderId: A602658300
  //      phoneNumber: 320-532-7407
  // todo where is the email?

  @Bean
  @Profile({"test", "default"})
  public Map<String, TribalNation> localTribalNations() {
    Map<String, TribalNation> result = new HashMap<>();
    result.put(MILLE_LACS_BAND_OF_OJIBWE, new TribalNation(
        MILLE_LACS_BAND_OF_OJIBWE, // TODO annoying but maybe the less bad option
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

  @Bean
  @Profile("production")
  public Map<String, TribalNation> prodTribalNations() {
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
