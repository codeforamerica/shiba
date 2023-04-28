package org.codeforamerica.shiba;

import static org.codeforamerica.shiba.internationalization.InternationalizationUtils.listToString;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.codeforamerica.shiba.internationalization.LocaleSpecificMessageSource;
import org.codeforamerica.shiba.mnit.RoutingDestination;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;


@Service
public class RoutingDestinationMessageService {

  private final MessageSource messageSource;

  public RoutingDestinationMessageService(MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  public String generatePhrase(Locale locale, County county, boolean withPhoneNumbers,
      List<RoutingDestination> routingDestinations) {
    LocaleSpecificMessageSource lms = new LocaleSpecificMessageSource(locale, messageSource);
    List<String> routingDestinationStrings = routingDestinations.stream().map(rd -> {
      if (rd instanceof TribalNationRoutingDestination) {
        return withPhoneNumbers ? rd.getName() + " (" + rd
            .getPhoneNumber() + ")":
            rd.getName();
      }
      if (!county.equals(County.Other)) {
	      String clientCounty = rd.getName();
	      return withPhoneNumbers ? lms
	          .getMessage("general.county-and-phone", List.of(clientCounty, rd.getPhoneNumber())) :
	          lms.getMessage("general.county", List.of(clientCounty));
      }
      return "";
    })
        .collect(Collectors.toList());
    return listToString(routingDestinationStrings, lms);
  }
}
