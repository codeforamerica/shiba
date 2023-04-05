package org.codeforamerica.shiba.mnit;

import static org.codeforamerica.shiba.TribalNation.BoisForte;
import static org.codeforamerica.shiba.TribalNation.FondDuLac;
import static org.codeforamerica.shiba.TribalNation.GrandPortage;
import static org.codeforamerica.shiba.TribalNation.LeechLake;
import static org.codeforamerica.shiba.TribalNation.MilleLacsBandOfOjibwe;
import static org.codeforamerica.shiba.TribalNation.RedLakeNation;
import static org.codeforamerica.shiba.TribalNation.WhiteEarth;

import org.codeforamerica.shiba.ServicingAgencyMap;
import org.codeforamerica.shiba.TribalNation;
import org.codeforamerica.shiba.TribalNationRoutingDestination;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class TribalNationConfiguration {

  @Bean
  @Profile({"test", "default"})
  public ServicingAgencyMap<TribalNationRoutingDestination> localTribalNations() {
    return getDefaultTribalNations();
  }

  @Bean
  @Profile({"dev"})
  public ServicingAgencyMap<TribalNationRoutingDestination> stagingTribalNations() {
    ServicingAgencyMap<TribalNationRoutingDestination> result = getDefaultTribalNations();
    addTribalNation(result, MilleLacsBandOfOjibwe,
        "A602658300",
        "help+staging@mnbenefits.org",
        "320-532-7407"
    );

    addTribalNation(result, WhiteEarth,
        "A086642300",
        "help+staging@mnbenefits.org",
        "218-935-2359"
    );
    addTribalNation(result, RedLakeNation,
        "A590043300",
        "help+staging@mnbenefits.org",
        "218-679-3350"
    );
    return result;
  }

  @Bean
  @Profile("demo")
  public ServicingAgencyMap<TribalNationRoutingDestination> demoTribalNations() {
    ServicingAgencyMap<TribalNationRoutingDestination> result = getDefaultTribalNations();
    addTribalNation(result,
        MilleLacsBandOfOjibwe,
        "A602658300",
        "help+demo@mnbenefits.org",
        "320-532-7407"
    );
    addTribalNation(result,
        WhiteEarth,
        "A086642300",
        "help+demo@mnbenefits.org",
        "218-935-2359"
    );
    addTribalNation(result, RedLakeNation,
        "A590043300",
        "help+demo@mnbenefits.org",
        "218-679-3350"
    );
    return result;
  }

  @Bean
  @Profile("production")
  public ServicingAgencyMap<TribalNationRoutingDestination> prodTribalNations() {
    ServicingAgencyMap<TribalNationRoutingDestination> result = new ServicingAgencyMap<>();
    addTribalNation(result, MilleLacsBandOfOjibwe,
        "A602658300",
        "candace.benjamin@millelacsband.com",
        "320-532-7407"
    );
    addTribalNation(result,
        WhiteEarth,
        "A086642300",
        "amy.littlewolf@whiteearth-nsn.gov",
        "218-935-2359"
    );
    addTribalNation(result, RedLakeNation,
        "A590043300",
        "sarah.smythe@redlakenation.org",
        "218-679-3350"
    );
    return result;
  }

  @NotNull
  private ServicingAgencyMap<TribalNationRoutingDestination> getDefaultTribalNations() {
    ServicingAgencyMap<TribalNationRoutingDestination> result = new ServicingAgencyMap<>();
    addTribalNation(result, MilleLacsBandOfOjibwe,
        "A602658300",
        "help+dev@mnbenefits.org",
        "320-532-7407"
    );
    addTribalNation(result, FondDuLac, "A590043300", "sarah.smythe@redlakenation.org",
        "218-679-3350");
    addTribalNation(result, GrandPortage, "A590043300", "sarah.smythe@redlakenation.org",
        "218-679-3350");
    addTribalNation(result, LeechLake, "A590043300", "sarah.smythe@redlakenation.org",
        "218-679-3350");
    addTribalNation(result, WhiteEarth,
        "A086642300",
        "help+dev@mnbenefits.org",
        "218-935-2359"
    );
    addTribalNation(result, BoisForte, "A590043300", "sarah.smythe@redlakenation.org",
        "218-679-3350");
    addTribalNation(result, RedLakeNation,
        "A590043300",
        "help+dev@mnbenefits.org",
        "218-679-3350"
    );
    return result;
  }

  private void addTribalNation(ServicingAgencyMap<TribalNationRoutingDestination> result,
      TribalNation tribalNation, String dhsProviderId, String email, String phoneNumber) {
    result.put(tribalNation,
        new TribalNationRoutingDestination(tribalNation, dhsProviderId, email, phoneNumber));
  }
}
