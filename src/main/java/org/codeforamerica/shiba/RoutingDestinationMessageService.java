package org.codeforamerica.shiba;

import static org.codeforamerica.shiba.internationalization.InternationalizationUtils.listToString;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.codeforamerica.shiba.internationalization.LocaleSpecificMessageSource;
import org.codeforamerica.shiba.mnit.RoutingDestination;
import org.springframework.context.MessageSource;

public class RoutingDestinationMessageService {
  private final MessageSource messageSource;

  public RoutingDestinationMessageService(MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  public String generateCcapMessageStrings(Locale locale, County county, List<RoutingDestination> routingDestinations) {
    return generatePhrase(locale, county, false, routingDestinations);
  }

  public String generateCafMessageStrings(Locale locale, County county, List<RoutingDestination> routingDestinations) {
    return generatePhrase(locale, county, true, routingDestinations);
  }

  public String generateSuccessPageMessageStrings(Locale locale, County county, List<RoutingDestination> routingDestinations) {
    return generatePhrase(locale, county, true, routingDestinations);
  }

  private String generatePhrase(Locale locale, County county, boolean withPhoneNumbers,
      List<RoutingDestination> routingDestinations) {
    LocaleSpecificMessageSource lms = new LocaleSpecificMessageSource(locale, messageSource);
    List<String> routingDestinationStrings = routingDestinations.stream().map(rd -> {
          if (rd instanceof TribalNationRoutingDestination) {
            return withPhoneNumbers ? rd.getName() + " Tribal Nation Servicing Agency " + rd.getPhoneNumber():
                rd.getName() + " Tribal Nation Servicing Agency";
          }
          String clientCounty = rd.getName();
          if (county == County.Other) {
            clientCounty = County.Hennepin.displayName();
          }
          return withPhoneNumbers ? lms.getMessage("general.county-and-phone", List.of(clientCounty, rd.getPhoneNumber())) :
              lms.getMessage("general.county", List.of(clientCounty));

        })
        .collect(Collectors.toSet())
        .stream().toList();
    // TODO need a test for different combinations of tribal nations and counties
    return listToString(routingDestinationStrings, lms);
  }
}
