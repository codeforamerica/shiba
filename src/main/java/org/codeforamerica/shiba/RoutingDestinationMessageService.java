package org.codeforamerica.shiba;

import static org.codeforamerica.shiba.internationalization.InternationalizationUtils.listToString;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.codeforamerica.shiba.internationalization.LocaleSpecificMessageSource;
import org.codeforamerica.shiba.mnit.CountyRoutingDestination;
import org.codeforamerica.shiba.mnit.RoutingDestination;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;


@Service
public class RoutingDestinationMessageService {

  private final MessageSource messageSource;

  public RoutingDestinationMessageService(MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  public List<String> generateCcapMessageStrings(Locale locale, County county,
      List<RoutingDestination> routingDestinations) {
    return List.of(generatePhraseWithCountyOnly(locale, county, false, routingDestinations),
        generatePhraseWithCountyOnly(locale, county, true, routingDestinations));
  }

  public List<String> generateCafMessageStrings(Locale locale, County county,
      List<RoutingDestination> routingDestinations) {
    return List.of(generatePhrase(locale, county, false, routingDestinations),
        generatePhrase(locale, county, true, routingDestinations));
  }

  public String generateSuccessPageMessageString(Locale locale, County county,
      List<RoutingDestination> routingDestinations) {
    return generatePhrase(locale, county, true, routingDestinations);
  }

  private String generatePhraseWithCountyOnly(Locale locale, County county,
      boolean withPhoneNumbers,
      List<RoutingDestination> routingDestinations) {
    LocaleSpecificMessageSource lms = new LocaleSpecificMessageSource(locale, messageSource);
    List<String> routingDestinationStrings = routingDestinations.stream()
        .filter(rd -> rd instanceof CountyRoutingDestination).map(rd -> {
          String clientCounty = setCountyName(county, rd);
          return withPhoneNumbers ? lms
              .getMessage("general.county-and-phone", List.of(clientCounty, rd.getPhoneNumber())) :
              lms.getMessage("general.county", List.of(clientCounty));
        })
        .collect(Collectors.toList());
    return listToString(routingDestinationStrings, lms);
  }

  private String generatePhrase(Locale locale, County county, boolean withPhoneNumbers,
      List<RoutingDestination> routingDestinations) {
    LocaleSpecificMessageSource lms = new LocaleSpecificMessageSource(locale, messageSource);
    List<String> routingDestinationStrings = routingDestinations.stream().map(rd -> {
      if (rd instanceof TribalNationRoutingDestination) {
        return withPhoneNumbers ? rd.getName() + " Tribal Nation Servicing Agency " + rd
            .getPhoneNumber() :
            rd.getName() + " Tribal Nation Servicing Agency";
      }
      String clientCounty = setCountyName(county, rd);
      return withPhoneNumbers ? lms
          .getMessage("general.county-and-phone", List.of(clientCounty, rd.getPhoneNumber())) :
          lms.getMessage("general.county", List.of(clientCounty));

    })
        .collect(Collectors.toList());
    return listToString(routingDestinationStrings, lms);
  }

  private String setCountyName(County county, RoutingDestination routingDestination) {
    String clientCounty = routingDestination.getName();
    if (county == County.Other) {
      clientCounty = County.Hennepin.displayName();
    }
    return clientCounty;
  }
}
